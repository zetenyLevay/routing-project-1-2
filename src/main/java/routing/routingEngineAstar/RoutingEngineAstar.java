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

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.builders.DynamicGraphBuilder;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

/**
 * AStarRouter with dynamic graph building and time constraints
 */
public class RoutingEngineAstar {

    private static final int MAX_WAIT_SECONDS = 1800;          // 1/2 hour
    // private static final double INITIAL_WALK_RADIUS_M = 1000; // 1 km
    private static final double SEARCH_RADIUS = 1000; // ~600 ft
    private static final boolean DEBUG = true;                 // Enable/disable debug output
    private static final double WALKING_SPEED_MPS = 1.3889;    // ~5 km/h in m/s

    private final DBConnectionManager dbManager;
    private final Map<String, Stop> allStops;
    private final DynamicGraphBuilder graphBuilder;

    public static void main(String[] args) {
        RoutingEngineAstar router = new RoutingEngineAstar(
                new DBConnectionManager("jdbc:sqlite:budapest_gtfs.db")
        );

        // Example: Vörösmarty tér → Köbánya-Kispest at 08:00:00
        double sourceLat = 47.5091742924609; 
        double sourceLon = 19.003668560505577;
        double destLat =   47.51860724748304; 
        double destLon =   19.09381377595606;
        String startTime = "17:00:00";

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
    }

    /**
     * Main routing method – finds a sequence of RouteStep from source to
     * destination.
     */
    public List<RouteStep> findRoute(double sourceLat, double sourceLon,
                                     double destLat, double destLon, String startTime) {
        debug("Finding route from (" + sourceLat + ", " + sourceLon
                + ") to (" + destLat + ", " + destLon + ") at " + startTime);

        // If source and destination are within walking distance, skip A* and walk
        double directDistance = TimeAndGeoUtils.haversineMeters(
                sourceLat, sourceLon, destLat, destLon);
        // if (directDistance <= INITIAL_WALK_RADIUS_M) {
        //     debug("Destination within walking distance (" +
        //           String.format("%.1f", directDistance) + " m). Walk directly.");
        //     return Collections.emptyList();
        // }

        // 1. Find nearby stops within INITIAL_WALK_RADIUS_M of source and destination
        List<Stop> startStops = findNearbyStops(sourceLat, sourceLon, SEARCH_RADIUS);
        List<Stop> endStops   = findNearbyStops(destLat,   destLon, SEARCH_RADIUS);

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

        // 2. Run the A* algorithm to get a List<RouteStep>
        return runAStar(startStops, endStops, startTime, sourceLat, sourceLon, destLat, destLon);
    }

    /**
     * Returns a list of “valid” RouteStep edges departing from `stop` at or
     * after `currentTime`. Delegates to DynamicGraphBuilder.
     */
    public List<RouteStep> getConnectionsForStop(Stop stop, String currentTime) {
        return graphBuilder.getValidRouteSteps(stop, currentTime);
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
                Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()
        ) {
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
     * Implements A* to find the optimal chain of RouteStep from any of the
     * startStops to any of the endStops, beginning no earlier than `startTime`.
     * Returns the list of RouteStep in forward order, or empty list if none.
     *
     * Note: - We assume that you can “arrive” at each startStop exactly at
     * startTime (i.e. zero walking penalty). - The heuristic is straight-line
     * distance to the nearest endStop, converted to seconds by assuming walking
     * at 1.3889 m/s. - We prune any edge whose wait (departureTime −
     * currentTime) exceeds MAX_WAIT_SECONDS.
     */
    private List<RouteStep> runAStar(
            List<Stop> startStops,
            List<Stop> endStops,
            String startTime,
            double sourceLat,
            double sourceLon,
            double destLat,
            double destLon
    ) {
        debug("Running A* algorithm...");

        // Convert “HH:mm:ss” → seconds since midnight
        int startTimeSec = timeToSeconds(startTime);

        // Build a Set of end‐stop IDs for quick membership test
        Set<String> endStopIds = new HashSet<>();
        for (Stop s : endStops) {
            endStopIds.add(s.getStopID());
        }

        // Data structures for A*:
        // 1) bestArrivalTime: stopId → earliest arrival time (in sec) seen so far
        Map<String, Integer> bestArrivalTime = new HashMap<>();
        // 2) cameFrom: stopId → predecessor stopId
        Map<String, String> cameFrom = new HashMap<>();
        // 3) cameStep: stopId → the RouteStep used to get there
        Map<String, RouteStep> cameStep = new HashMap<>();

        // Priority queue (min‐heap) of Node, ordered by fScore = arrivalTime + heuristic.
        PriorityQueue<Node> openSet = new PriorityQueue<>();

        // Initialize: for each startStop, “arrive” exactly at startTimeSec
        for (Stop startStop : startStops) {
            String sid = startStop.getStopID();
            int arrival = startTimeSec;
            double h = heuristic(startStop, endStops);
            double f = arrival + h;

            bestArrivalTime.put(sid, arrival);
            openSet.add(new Node(startStop, arrival, f));
            // No cameFrom / cameStep for a startStop (that is the root).
        }

        // A* main loop
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            Stop currStop = current.stop;
            int currArrSec = current.arrivalTime;
            String currId = currStop.getStopID();

            // If this node’s arrival time is worse than the best known, skip it
            if (bestArrivalTime.containsKey(currId)
                    && currArrSec > bestArrivalTime.get(currId)) {
                continue;
            }

            // If we’ve reached any end‐stop, reconstruct path
            if (endStopIds.contains(currId)) {
                debug("Reached end stop " + currId
                        + " at time " + secondsToTime(currArrSec));
                return reconstructPath(currId, cameFrom, cameStep);
            }

            // Expand neighbors: get all valid RouteStep edges from currStop at currArrSec
            String currTimeStr = secondsToTime(currArrSec);
            List<RouteStep> neighbors = getConnectionsForStop(currStop, currTimeStr);
            debug("Expanding " + currId + " @ " + currTimeStr
                    + " → found " + neighbors.size() + " neighbors");

            for (RouteStep step : neighbors) {
                // Assume RouteStep has:
                //   step.getToStop()       → Stop object of the next stop
                //   step.getDepartureTime() → String "HH:mm:ss"
                //   step.getArrivalTime()   → String "HH:mm:ss"
                Stop nextStop = step.getToStop();
                String nextId = nextStop.getStopID();

                // Parse departure and arrival times
                int depSec = timeToSeconds(step.getDepartureTime());
                int arrSec = timeToSeconds(step.getArrivalTime());

                // Skip any edge where arrival is before departure
                if (arrSec < depSec) {
                    continue;
                }

                // If departure is before current arrival, skip
                if (depSec < currArrSec) {
                    continue;
                }
                // If waiting time is too large, skip
                if (depSec - currArrSec > MAX_WAIT_SECONDS) {
                    continue;
                }
                // If this arrival time is better than any known:
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

        // If we exit the loop, no route was found
        debug("A* search exhausted – no route found");
        return Collections.emptyList();
    }

    /**
     * Reconstructs the list of RouteStep from the startStop to destStopId. We
     * stored, for each stopId, the predecessor stopId in `cameFrom`, and the
     * RouteStep used in `cameStep`.
     */
    private List<RouteStep> reconstructPath(
            String destStopId,
            Map<String, String> cameFrom,
            Map<String, RouteStep> cameStep
    ) {
        LinkedList<RouteStep> path = new LinkedList<>();
        String currentId = destStopId;

        // Walk backwards until we reach a stop that has no predecessor (one of the startStops)
        while (cameFrom.containsKey(currentId)) {
            RouteStep step = cameStep.get(currentId);
            path.addFirst(step);
            currentId = cameFrom.get(currentId);
        }
        return path;
    }

    /**
     * Heuristic: straight-line (haversine) distance from currentStop to the
     * nearest endStop, converted to “seconds remaining” by dividing by walking
     * speed.
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
        // Convert to seconds at walking speed
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
     * Converts total seconds since midnight back to "HH:mm:ss". If hours exceed
     * 23, we still format as e.g. "24:15:00" for 24h15m.
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
     * Node used in the A* PriorityQueue. Each node represents “being at Stop
     * stop at time arrivalTime (in seconds)”, and is prioritized by fScore =
     * arrivalTime + heuristicEstimate.
     */
    private class Node implements Comparable<Node> {

        final Stop stop;
        final int arrivalTime;  // in seconds since midnight
        final double fScore;    // arrivalTime + heuristic

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
}
