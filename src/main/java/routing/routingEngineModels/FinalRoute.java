//list of RouteSteps

package routing.routingEngineModels;


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

    public FinalRoute(Object routeSteps2, double totalDistance2, double totalTime2) {
        //TODO Auto-generated constructor stub
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
