package routing.routingEngineDijkstra.newDijkstra.model.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinalRoute {
    private final List<RouteStep> routeSteps;
    private final double totalDistance;
    private final double totalTime;

    public FinalRoute(List<RouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = Collections.unmodifiableList(new ArrayList<>(routeSteps));
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public List<RouteStep> getRouteSteps() {
        return routeSteps;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getAverageSpeed() {
        return totalTime > 0 ? totalDistance / totalTime : 0;
    }
}