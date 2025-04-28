//collection of RouteSteps

package RoutingEngineModels;
import RoutingEngineDijkstra.Models.RouteStep;

import java.util.ArrayList;

public class FinalRoute{

    private ArrayList<RouteStep> routeSteps;
    private double totalDistance;
    private double totalTime;

    public FinalRoute(ArrayList<RouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = routeSteps;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public ArrayList<RouteStep> getRouteSteps() {
        return this.routeSteps;
    }

    public double getTotalDistance() {
        return this.totalDistance;
    }

    public double getTotalTime() {
        return this.totalTime;
    }

}
