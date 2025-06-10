//list of RouteSteps

package routing.routingEngineModels;
//import Routing.RoutingEngineModels.RouteStep;

import java.util.ArrayList;

import routing.routingEngineDijkstra.adiModels.AdiRouteStep;

/**
 * FinalRoute.java
 *
 * Represents the final route computed by the routing engine. It contains a list
 * of route steps, total distance, and total time for the route.
 */
public class FinalRoute{

    private ArrayList<AdiRouteStep> routeSteps;
    private double totalDistance;
    private double totalTime;

    /**
     * Constructor for FinalRoute.
     *
     * @param routeSteps List of AdiRouteStep objects representing the steps in the route.
     * @param totalDistance Total distance of the route in meters.
     * @param totalTime Total time of the route in seconds.
     */
    public FinalRoute(ArrayList<AdiRouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = routeSteps;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    // Getters
    public ArrayList<AdiRouteStep> getRouteSteps() {
        return this.routeSteps;
    }

    public double getTotalDistance() {
        return this.totalDistance;
    }

    public double getTotalTime() {
        return this.totalTime;
    }

}
