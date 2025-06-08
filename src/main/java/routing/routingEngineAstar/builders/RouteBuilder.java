package routing.routingEngineAstar.builders;

import java.util.ArrayList;
import java.util.List;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

public class RouteBuilder {
    private double WALKING_SPEED_MPS;
    

    public RouteBuilder(double walkingSpeedMps) {
        this.WALKING_SPEED_MPS = walkingSpeedMps;
    }

     /**
     * Adds an initial walking step to the given boardingStop, then all transit
     * steps, then a final walking step from the last alight stop to the
     * destination.
     */
    public List<RouteStep> addWalkingSteps(List<RouteStep> transitRoute,
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
    public List<RouteStep> mergeConsecutiveWalks(List<RouteStep> route,
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
     * Creates a direct walking route when source and destination are close.
     */
    public List<RouteStep> createDirectWalkingRoute(double sourceLat, double sourceLon,
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
}
