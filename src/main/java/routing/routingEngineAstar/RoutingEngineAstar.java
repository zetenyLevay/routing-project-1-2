package routing.routingEngineAstar;

import java.sql.Connection;
import java.sql.SQLException;
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
import routing.routingEngineAstar.builders.RouteBuilder;
import routing.routingEngineAstar.finders.StopService;
import routing.routingEngineAstar.miscellaneous.Node;
import routing.routingEngineAstar.miscellaneous.TimeUtils;
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
    private static final double WALKING_SPEED_MPS = 1.3889;     // ~5 km/h in m/s

    @SuppressWarnings("unused")
    private final DBConnectionManager dbManager;
    private final Map<String, Stop> allStops;
    private final DynamicGraphBuilder graphBuilder;
    private RouteBuilder routeBuilder;
    private StopService stopService;


    /**
     * Constructor for RoutingEngineAstar.
     *
     * @param dbManager the database connection manager
     */
    public RoutingEngineAstar(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.routeBuilder = new RouteBuilder(WALKING_SPEED_MPS);
        this.stopService = new StopService(dbManager);
        this.allStops = stopService.getAllStops(); 

        this.graphBuilder = new DynamicGraphBuilder(dbManager);

        // ensure our stop_times indexes exist
        try (Connection conn = dbManager.getConnection()) {
            ZipToSQLite.createIndexes("stop_times.txt", conn);
        } catch (SQLException e) {
            // either log or rethrow as unchecked
            throw new RuntimeException("Unable to create indexes", e);
        }

    }

    /**
     * Finds a route from source to destination, using A* search with dynamic
     * graph building and time constraints.
     *
     * @param sourceLat  latitude of the source location
     * @param sourceLon  longitude of the source location
     * @param destLat    latitude of the destination location
     * @param destLon    longitude of the destination location
     * @param startTime  starting time in "HH:mm:ss" format
     * @return a list of RouteStep representing the route
     */
    public List<RouteStep> findRoute(double sourceLat, double sourceLon,
            double destLat, double destLon, String startTime) {

        // 1) If source and destination are within walking distance, return direct walk.
        double directDistance = TimeAndGeoUtils.haversineMeters(
                sourceLat, sourceLon, destLat, destLon);
        if (directDistance <= INITIAL_WALK_RADIUS_M) {
            return routeBuilder.createDirectWalkingRoute(sourceLat, sourceLon, destLat, destLon, startTime);
        }

        // 2) Find nearby stops within SEARCH_RADIUS of source and destination.
        List<Stop> startStops = stopService.findNearbyStops(sourceLat, sourceLon, SEARCH_RADIUS);
        List<Stop> endStops = stopService.findNearbyStops(destLat, destLon, SEARCH_RADIUS);


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
            return routeBuilder.createDirectWalkingRoute(sourceLat, sourceLon, destLat, destLon, startTime);
        }

        // 4) Wrap transit steps with walking‐to and walking‐from, then merge consecutive walks.
        List<RouteStep> withWalks = routeBuilder.addWalkingSteps(
                transitRoute, boardingStop,
                sourceLat, sourceLon,
                destLat, destLon,
                startTime
        );
        return routeBuilder.mergeConsecutiveWalks(withWalks, sourceLat, sourceLon, destLat, destLon);
        
    }   

    /**
     * A simple container for the result of the A* search.
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
     * Runs the A* search algorithm to find the best route from startStops to
     * endStops, considering the given start time and source/destination coordinates.
     *
     * @param startStops  list of starting stops
     * @param endStops    list of ending stops
     * @param startTime   starting time in "HH:mm:ss" format
     * @param sourceLat   latitude of the source location
     * @param sourceLon   longitude of the source location
     * @param destLat     latitude of the destination location
     * @param destLon     longitude of the destination location
     * @return an AStarResult containing the route steps and first boarding stop
     */
    private AStarResult runAStar(List<Stop> startStops, List<Stop> endStops, String startTime, double sourceLat, double sourceLon, double destLat, double destLon) {

        // Convert "HH:mm:ss" → seconds since midnight
        int startTimeSec = TimeUtils.timeToSeconds(startTime);

        // Build a Set of end‐stop IDs for quick membership test
        Set<String> endStopIds = new HashSet<>();
        for (Stop s : endStops) {
            endStopIds.add(s.getStopID());
        }

        // Data structures for A*:
        Map<String, Integer> bestArrivalTime = new HashMap<>();
        Map<String, String> cameFrom = new HashMap<>();
        Map<String, RouteStep> cameStep = new HashMap<>();

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

                // Reconstruct the list of RouteSteps
                List<RouteStep> path = reconstructPath(currId, cameFrom, cameStep);

                // We know path is non-empty here
                String firstToId = path.get(0).getToStop().getStopID();
                String firstFromId = cameFrom.get(firstToId);
                Stop firstBoardStop = allStops.get(firstFromId);

                return new AStarResult(path, firstBoardStop);
            }

            // Otherwise, expand neighbors from currStop at currTimeStr
            String currTimeStr = TimeUtils.secondsToTime(currArrSec);
            List<RouteStep> neighbors = getConnectionsForStop(currStop, currTimeStr);

            for (RouteStep step : neighbors) {
                Stop nextStop = step.getToStop();
                String nextId = nextStop.getStopID();

                int depSec = TimeUtils.timeToSeconds(step.getDepartureTime());
                int arrSec = TimeUtils.timeToSeconds(step.getArrivalTime());

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

                }
            }
        }
        // No route found → return empty steps and null boarding stop
        return new AStarResult(Collections.emptyList(), null);
    }

    /**
     * Reconstructs the path from the destination stop back to the source stop
     * using the cameFrom and cameStep maps.
     *
     * @param destStopId  ID of the destination stop
     * @param cameFrom    map of stop IDs to their predecessor stop IDs
     * @param cameStep    map of stop IDs to their corresponding RouteSteps
     * @return a list of RouteStep representing the reconstructed path
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
     * Heuristic function for A* search: calculates the minimum distance from
     * the current stop to any of the end stops, divided by walking speed.
     *
     * @param currentStop  the current stop being evaluated
     * @param endStops     list of destination stops
     * @return estimated time in seconds to reach any end stop from the current stop
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
     * Retrieves all valid connections for a given stop at the specified time.
     *
     * @param stop         the stop to get connections for
     * @param currentTime  the current time in "HH:mm:ss" format
     * @return a list of RouteStep representing valid connections from the stop
     */
    public List<RouteStep> getConnectionsForStop(Stop stop, String currentTime) {
        return graphBuilder.getValidRouteSteps(stop, currentTime);
    }
}
