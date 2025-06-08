//list of RouteSteps

package routing.routingEngineModels;
//import Routing.RoutingEngineModels.RouteStep;

import routing.routingEngineDijkstra.adiModels.AdiRouteStep;

import java.util.ArrayList;

public class FinalRoute{

    private ArrayList<AdiRouteStep> routeSteps;
    private double totalDistance;
    private double totalTime;

    public FinalRoute(ArrayList<AdiRouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = routeSteps;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

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
