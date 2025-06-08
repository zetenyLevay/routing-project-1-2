package routing.routingEngineAstar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import parsers.ZipToSQLite;
import routing.db.DBConnectionManager;
import routing.routingEngineAstar.builders.DynamicGraphBuilder;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * AStarRouter with dynamic graph building and time constraints.
 *
 * Changes: 1. If A* finds no transit steps, fall back to a single “WALK” step.
 * 2. Merge any consecutive walk legs in the final route into one direct walk
 * (so you don’t walk to an intermediate stop and then walk again).
 */
public class RoutingEngineAstar {

    private static final int MAX_WAIT_SECONDS = 1800;     // 30 mins
    private static final double INITIAL_WALK_RADIUS_M = 1000;    // 300 m
    private static final double SEARCH_RADIUS = 1000;           // ~1 km
    private static final boolean DEBUG = false;                  // Enable/disable debug output
    private static final double WALKING_SPEED_MPS = 1.3889;     // ~5 km/h in m/s

    private final DBConnectionManager dbManager;
    private final Map<String, Stop> allStops;
    private final DynamicGraphBuilder graphBuilder;

    public static void main(String[] args) {
        RoutingEngineAstar router = new RoutingEngineAstar(
                new DBConnectionManager("jdbc:sqlite:budapest_gtfs.db")
        );

        // Example: two farther‐apart points
        double sourceLat = 47.498333190458226; //source point
        double sourceLon = 19.074383183671998; //source point 2
        double destLat = 47.49563896935584; // destination point
        double destLon = 19.035322782272477; // destination point 2
        String startTime = "18:54:00";

        System.out.println("Testing route finding...");
        List<RouteStep> route = router.findRoute(sourceLat, sourceLon, destLat, destLon, startTime);

        System.out.println("Found route with " + route.size() + " steps:");
        for (int i = 0; i < route.size(); i++) {
            System.out.println("Step " + (i + 1) + ": " + route.get(i));
        }
    }

    public RoutingEngineAstar(DBConnectionManager dbManager) {
        this.dbManager = dbManager;

        debug("Loading all stops...");
        this.allStops = loadAllStops();
        debug("Loaded " + allStops.size() + " stops");

        this.graphBuilder = new DynamicGraphBuilder(dbManager);

        // ensure our stop_times indexes exist
        try (Connection conn = dbManager.getConnection()) {
            ZipToSQLite.createIndexes("stop_times.txt", conn);
        } catch (SQLException e) {
            // either log or rethrow as unchecked
            throw new RuntimeException("Unable to create indexes", e);
        }

    }

// {"routeFrom":{"lat":50.850781642439834,"lon":5.69112308868195},"startingAt":"21:13","to":{"lat":50.838823893023566,"lon":5.712222386520314}}
    /**
     * Main routing method – finds a sequence of RouteStep from source to
     * destination.
     */
    public List<RouteStep> findRoute(double sourceLat, double sourceLon,
            double destLat, double destLon, String startTime) {
        debug("Finding route from (" + sourceLat + ", " + sourceLon
                + ") to (" + destLat + ", " + destLon + ") at " + startTime);

        // 1) If source and destination are within walking distance, return direct walk.
        double directDistance = TimeAndGeoUtils.haversineMeters(
                sourceLat, sourceLon, destLat, destLon);
        if (directDistance <= INITIAL_WALK_RADIUS_M) {
            debug("Destination within walking distance ("
                    + String.format("%.1f", directDistance) + " m). Walk directly.");
            return createDirectWalkingRoute(sourceLat, sourceLon, destLat, destLon, startTime);
        }

        // 2) Find nearby stops within SEARCH_RADIUS of source and destination.
        List<Stop> startStops = findNearbyStops(sourceLat, sourceLon, SEARCH_RADIUS);
        List<Stop> endStops = findNearbyStops(destLat, destLon, SEARCH_RADIUS);

        debug("Found " + startStops.size() + " possible start stops and "
                + endStops.size() + " possible end stops");

        if (DEBUG && !startStops.isEmpty()) {
            debug("Start stops (up to 3):");
            for (int i = 0; i < Math.min(3, startStops.size()); i++) {
                Stop stop = startStops.get(i);
                double dist = TimeAndGeoUtils.haversineMeters(
                        sourceLat, sourceLon,
                        stop.getLatitude(), stop.getLongitude()
                );
                debug("  • " + stop.getStopID() + ": " + stop.getStopName()
                        + " (" + String.format("%.1f", dist) + " m away)");
            }
        }
        if (DEBUG && !endStops.isEmpty()) {
            debug("End stops (up to 3):");
            for (int i = 0; i < Math.min(3, endStops.size()); i++) {
                Stop stop = endStops.get(i);
                double dist = TimeAndGeoUtils.haversineMeters(
                        destLat, destLon,
                        stop.getLatitude(), stop.getLongitude()
                );
                debug("  • " + stop.getStopID() + ": " + stop.getStopName()
                        + " (" + String.format("%.1f", dist) + " m away)");
            }
        }

        if (startStops.isEmpty()) {
            System.err.println("No start stops found within " + SEARCH_RADIUS + " m");
            return Collections.emptyList();
        }
        if (endStops.isEmpty()) {
            System.err.println("No end stops found within " + SEARCH_RADIUS + " m");
            return Collections.emptyList();
        }

        // 3) Run A* to get both the list of RouteStep and the actual boarding stop.
        AStarResult result = runAStar(
                startStops, endStops, startTime, sourceLat, sourceLon, destLat, destLon
        );

        List<RouteStep> transitRoute = result.steps;
        Stop boardingStop = result.firstBoardStop;

        // If A* found no transit path, fall back to walking the entire distance.
        if (transitRoute.isEmpty()) {
            debug("No transit route found; falling back to walking the entire "
                    + String.format("%.1f", directDistance) + " m.");
            return createDirectWalkingRoute(sourceLat, sourceLon, destLat, destLon, startTime);
        }

        // 4) Wrap transit steps with walking‐to and walking‐from, then merge consecutive walks.
        List<RouteStep> withWalks = addWalkingSteps(
                transitRoute, boardingStop,
                sourceLat, sourceLon,
                destLat, destLon,
                startTime
        );
        return mergeConsecutiveWalks(withWalks, sourceLat, sourceLon, destLat, destLon);
    }

    /**
     * Creates a direct walking route when source and destination are close.
     */
    private List<RouteStep> createDirectWalkingRoute(double sourceLat, double sourceLon,
            double destLat, double destLon, String startTime) {
        List<RouteStep> route = new ArrayList<>();

        // Create a virtual destination stop
        Coordinates destCoords = new Coordinates(destLat, destLon);
        Stop virtualDestStop = new Stop("DEST", "Destination", destCoords);

        // Calculate walking time
        double distance = TimeAndGeoUtils.haversineMeters(sourceLat, sourceLon, destLat, destLon);
        int walkingSeconds = (int) Math.ceil(distance / WALKING_SPEED_MPS);

        // Create and return a single walking step
        RouteStep walkStep = new RouteStep("walk", virtualDestStop, startTime, walkingSeconds);
        route.add(walkStep);

        return route;
    }

    /**
     * Adds an initial walking step to the given boardingStop, then all transit
     * steps, then a final walking step from the last alight stop to the
     * destination.
     */
    private List<RouteStep> addWalkingSteps(List<RouteStep> transitRoute,
            Stop boardingStop,
            double sourceLat, double sourceLon,
            double destLat, double destLon,
            String startTime) {
        if (transitRoute.isEmpty()) {
            return transitRoute;
        }

        List<RouteStep> completeRoute = new ArrayList<>();

        // 1) Walk from source to the actual boarding stop
        if (boardingStop != null) {
            double walkDistance = TimeAndGeoUtils.haversineMeters(
                    sourceLat, sourceLon,
                    boardingStop.getLatitude(), boardingStop.getLongitude()
            );
            if (walkDistance > 10) { // Only add if more than 10 m
                int walkingSeconds = (int) Math.ceil(walkDistance / WALKING_SPEED_MPS);
                RouteStep initialWalk = new RouteStep("walk", boardingStop, startTime, walkingSeconds);
                completeRoute.add(initialWalk);
                debug("Added initial walking step: "
                        + String.format("%.1f", walkDistance) + " m to " + boardingStop.getStopName());
            }
        }

        // 2) Add all transit steps in the order returned by A*
        completeRoute.addAll(transitRoute);

        // 3) Walk from the last transit stop to the final destination
        RouteStep lastTransitStep = transitRoute.get(transitRoute.size() - 1);
        Stop lastTransitStop = lastTransitStep.getToStop();

        double finalWalkDistance = TimeAndGeoUtils.haversineMeters(
                lastTransitStop.getLatitude(), lastTransitStop.getLongitude(),
                destLat, destLon
        );
        if (finalWalkDistance > 10) { // Only add if more than 10 m
            Coordinates destCoords = new Coordinates(destLat, destLon);
            Stop virtualDestStop = new Stop("DEST", "Destination", destCoords);

            int finalWalkingSeconds = (int) Math.ceil(finalWalkDistance / WALKING_SPEED_MPS);
            String lastArrivalTime = lastTransitStep.getArrivalTime();

            RouteStep finalWalk = new RouteStep("walk", virtualDestStop, lastArrivalTime, finalWalkingSeconds);
            completeRoute.add(finalWalk);
            debug("Added final walking step: "
                    + String.format("%.1f", finalWalkDistance)
                    + " m from " + lastTransitStop.getStopName());
        }

        return completeRoute;
    }

    /**
     * Scans the route and merges any consecutive walk legs into a single direct
     * walk.
     *
     * For each maximal run of consecutive walk steps (indices [j..k]), it: -
     * Computes fromCoords = (if j == 0) (sourceLat, sourceLon) else the ToStop
     * coordinates of step (j-1). - Computes toCoords = the ToStop coordinates
     * of step k. - departureTime = departureTime of step j. - duration = ceil(
     * haversine(fromCoords, toCoords) / WALKING_SPEED_MPS ). - Replaces the
     * entire run [j..k] with one new walk step.
     */
    private List<RouteStep> mergeConsecutiveWalks(List<RouteStep> route,
            double sourceLat, double sourceLon,
            double destLat, double destLon) {
        if (route.isEmpty()) {
            return route;
        }

        List<RouteStep> merged = new ArrayList<>();
        int n = route.size();
        int i = 0;

        while (i < n) {
            RouteStep step = route.get(i);
            if (!step.getModeOfTransport().equals("walk")) {
                // Non-walking step—copy as is
                merged.add(step);
                i++;
                continue;
            }

            // Found the start of a run of WALKs
            int j = i;
            // Find k = last index of this consecutive run of walk
            int k = j;
            while (k + 1 < n && route.get(k + 1).getModeOfTransport().equals("walk")) {
                k++;
            }

            // Determine the “from” coordinates:
            double fromLat, fromLon;
            if (j == 0) {
                // First step in route is a walk: from = source
                fromLat = sourceLat;
                fromLon = sourceLon;
            } else {
                // from = the toStop of route.get(j-1)
                Stop prevStop = route.get(j - 1).getToStop();
                fromLat = prevStop.getLatitude();
                fromLon = prevStop.getLongitude();
            }

            // Determine the “to” Stop and its coordinates:
            Stop finalStopInRun = route.get(k).getToStop();
            double toLat = finalStopInRun.getLatitude();
            double toLon = finalStopInRun.getLongitude();

            // departureTime = route.get(j).getDepartureTime()
            String departureTime = route.get(j).getDepartureTime();

            // Compute direct walking time
            double totalDistance = TimeAndGeoUtils.haversineMeters(fromLat, fromLon, toLat, toLon);
            int walkingSeconds = (int) Math.ceil(totalDistance / WALKING_SPEED_MPS);

            // Create a single merged walk step
            RouteStep mergedWalk = new RouteStep("walk", finalStopInRun, departureTime, walkingSeconds);
            merged.add(mergedWalk);

            // Advance i to k+1 (skip over the entire run)
            i = k + 1;
        }

        return merged;
    }

    /**
     * A small container for the result of runAStar: - steps: the sequence of
     * RouteStep (in forward order) - firstBoardStop: the Stop where the first
     * step boards
     */
    private static class AStarResult {

        final List<RouteStep> steps;
        final Stop firstBoardStop;

        AStarResult(List<RouteStep> steps, Stop firstBoardStop) {
            this.steps = steps;
            this.firstBoardStop = firstBoardStop;
        }
    }

    /**
     * Implements A* to find the optimal chain of RouteStep from any of the
     * startStops to any of the endStops, beginning no earlier than `startTime`.
     * Returns both the list of RouteStep and the Stop where the first step
     * boards.
     */
    private AStarResult runAStar(
            List<Stop> startStops,
            List<Stop> endStops,
            String startTime,
            double sourceLat,
            double sourceLon,
            double destLat,
            double destLon
    ) {
        debug("Running A* algorithm...");

        // Convert "HH:mm:ss" → seconds since midnight
        int startTimeSec = timeToSeconds(startTime);

        // Build a Set of end‐stop IDs for quick membership test
        Set<String> endStopIds = new HashSet<>();
        for (Stop s : endStops) {
            endStopIds.add(s.getStopID());
        }

        // Data structures for A*:
        Map<String, Integer> bestArrivalTime = new HashMap<>();        // stopID → best known arrival in seconds
        Map<String, String> cameFrom = new HashMap<>();                 // stopID → predecessor stopID
        Map<String, RouteStep> cameStep = new HashMap<>();              // stopID → RouteStep used to arrive here

        PriorityQueue<Node> openSet = new PriorityQueue<>();

        // Initialize: for each startStop, account for walking time from source
        for (Stop startStop : startStops) {
            String sid = startStop.getStopID();

            double walkDistance = TimeAndGeoUtils.haversineMeters(
                    sourceLat, sourceLon,
                    startStop.getLatitude(), startStop.getLongitude()
            );
            int walkingSeconds = (int) Math.ceil(walkDistance / WALKING_SPEED_MPS);
            int arrival = startTimeSec + walkingSeconds;

            double h = heuristic(startStop, endStops);
            double f = arrival + h;

            bestArrivalTime.put(sid, arrival);
            openSet.add(new Node(startStop, arrival, f));
        }

        // A* main loop
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            Stop currStop = current.stop;
            int currArrSec = current.arrivalTime;
            String currId = currStop.getStopID();

            // Skip if we have already found a better arrival for this stop
            if (bestArrivalTime.containsKey(currId)
                    && currArrSec > bestArrivalTime.get(currId)) {
                continue;
            }

            // If this stop is one of the endStops, reconstruct the path
            if (endStopIds.contains(currId) && cameFrom.containsKey(currId)) {
                debug("Reached end stop " + currId
                        + " at time " + secondsToTime(currArrSec));

                // Reconstruct the list of RouteSteps
                List<RouteStep> path = reconstructPath(currId, cameFrom, cameStep);

                // We know path is non-empty here
                String firstToId = path.get(0).getToStop().getStopID();
                String firstFromId = cameFrom.get(firstToId);
                Stop firstBoardStop = allStops.get(firstFromId);

                return new AStarResult(path, firstBoardStop);
            }

            // Otherwise, expand neighbors from currStop at currTimeStr
            String currTimeStr = secondsToTime(currArrSec);
            List<RouteStep> neighbors = getConnectionsForStop(currStop, currTimeStr);
            debug("Expanding " + currId + " @ " + currTimeStr
                    + " → found " + neighbors.size() + " neighbors");

            for (RouteStep step : neighbors) {
                Stop nextStop = step.getToStop();
                String nextId = nextStop.getStopID();

                int depSec = timeToSeconds(step.getDepartureTime());
                int arrSec = timeToSeconds(step.getArrivalTime());

                // Skip invalid connections or excessive waiting
                if (arrSec < depSec || depSec < currArrSec || depSec - currArrSec > MAX_WAIT_SECONDS) {
                    continue;
                }

                int bestKnown = bestArrivalTime.getOrDefault(nextId, Integer.MAX_VALUE);
                if (arrSec < bestKnown) {
                    bestArrivalTime.put(nextId, arrSec);
                    cameFrom.put(nextId, currId);
                    cameStep.put(nextId, step);

                    double hNext = heuristic(nextStop, endStops);
                    double fNext = arrSec + hNext;
                    openSet.add(new Node(nextStop, arrSec, fNext));

                    debug("  → Relaxed edge " + currId + "→" + nextId
                            + " | dep=" + step.getDepartureTime()
                            + " arr=" + step.getArrivalTime()
                            + " | newBest=" + secondsToTime(arrSec));
                }
            }
        }

        debug("A* search exhausted – no route found");
        // No route found → return empty steps and null boarding stop
        return new AStarResult(Collections.emptyList(), null);
    }

    /**
     * Reconstructs the list of RouteStep from the startStop to destStopId.
     */
    private List<RouteStep> reconstructPath(
            String destStopId,
            Map<String, String> cameFrom,
            Map<String, RouteStep> cameStep
    ) {
        LinkedList<RouteStep> path = new LinkedList<>();
        String currentId = destStopId;

        while (cameFrom.containsKey(currentId)) {
            RouteStep step = cameStep.get(currentId);
            path.addFirst(step);
            currentId = cameFrom.get(currentId);
        }
        return path;
    }

    /**
     * Heuristic: straight-line distance converted to walking time.
     */
    private double heuristic(Stop currentStop, List<Stop> endStops) {
        double minDist = Double.MAX_VALUE;
        double lat1 = currentStop.getLatitude();
        double lon1 = currentStop.getLongitude();

        for (Stop end : endStops) {
            double d = TimeAndGeoUtils.haversineMeters(
                    lat1, lon1,
                    end.getLatitude(), end.getLongitude()
            );
            if (d < minDist) {
                minDist = d;
            }
        }
        return minDist / WALKING_SPEED_MPS;
    }

    /**
     * Converts a time string "HH:mm:ss" to total seconds since midnight.
     */
    private int timeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }

    /**
     * Converts total seconds since midnight back to "HH:mm:ss".
     */
    private String secondsToTime(int totalSeconds) {
        int h = totalSeconds / 3600;
        int rem = totalSeconds % 3600;
        int m = rem / 60;
        int s = rem % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Simple wrapper to print debug messages when DEBUG=true.
     */
    private void debug(String message) {
        if (DEBUG) {
            System.out.println("[DEBUG] " + message);
        }
    }

    /**
     * Node used in the A* PriorityQueue.
     */
    private class Node implements Comparable<Node> {

        final Stop stop;
        final int arrivalTime;   // seconds since midnight
        final double fScore;

        Node(Stop stop, int arrivalTime, double fScore) {
            this.stop = stop;
            this.arrivalTime = arrivalTime;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    /**
     * Loads all stops from the GTFS `stops` table into a HashMap, keyed by
     * stop_id.
     */
    private Map<String, Stop> loadAllStops() {
        Map<String, Stop> stops = new HashMap<>();

        String query = """
            SELECT stop_id, stop_name, stop_lat, stop_lon
            FROM stops
            """;

        try (
                Connection conn = dbManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");

                Coordinates coordinates = new Coordinates(lat, lon);
                Stop stop = new Stop(stopId, stopName, coordinates);
                stops.put(stopId, stop);
            }
        } catch (SQLException e) {
            System.err.println("Error loading stops: " + e.getMessage());
            e.printStackTrace();
        }

        return stops;
    }

    /**
     * Finds all stops within `radiusM` meters of (lat, lon), using
     * straight‐line (haversine) distance.
     */
    private List<Stop> findNearbyStops(double lat, double lon, double radiusM) {
        List<Stop> nearbyStops = new ArrayList<>();

        debug("Searching for stops within " + radiusM + " m of (" + lat + ", " + lon + ")");

        for (Stop stop : allStops.values()) {
            double distance = TimeAndGeoUtils.haversineMeters(
                    lat, lon,
                    stop.getLatitude(), stop.getLongitude()
            );
            if (distance <= radiusM) {
                nearbyStops.add(stop);
                if (DEBUG && nearbyStops.size() <= 5) {
                    debug("  • Candidate: " + stop.getStopID() + " at "
                            + String.format("%.1f", distance) + " m");
                }
            }
        }

        debug("Total nearby stops found: " + nearbyStops.size());
        return nearbyStops;
    }

    /**
     * Delegates to DynamicGraphBuilder to get valid RouteSteps from a stop at a
     * given time.
     */
    public List<RouteStep> getConnectionsForStop(Stop stop, String currentTime) {
        return graphBuilder.getValidRouteSteps(stop, currentTime);
    }
}
