package routing.routingEngineDijkstra.dijkstra.model.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the final calculated route, consisting of route steps, total distance, and total time.
 */
public class DijkstraFinalRoute {
    private final List<DijkstraRouteStep> routeSteps;
    private final double totalDistance;
    private final double totalTime;

    /**
     * Constructs a DijkstraFinalRoute with the specified steps, distance, and time.
     *
     * @param routeSteps    the list of route steps
     * @param totalDistance the total distance of the route in meters
     * @param totalTime     the total time of the route in seconds
     */
    public DijkstraFinalRoute(List<DijkstraRouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = Collections.unmodifiableList(new ArrayList<>(routeSteps));
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    /**
     * Retrieves the list of route steps.
     *
     * @return an unmodifiable list of DijkstraRouteStep objects
     */
    public List<DijkstraRouteStep> getRouteSteps() {
        return routeSteps;
    }

    /**
     * Retrieves the total distance of the route.
     *
     * @return the total distance in meters
     */
    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * Retrieves the total time of the route.
     *
     * @return the total time in seconds
     */
    public double getTotalTime() {
        return totalTime;
    }

    /**
     * Calculates the average speed of the route.
     *
     * @return the average speed in meters per second, or 0 if total time is 0
     */
    public double getAverageSpeed() {
        return totalTime > 0 ? totalDistance / totalTime : 0;
    }
}