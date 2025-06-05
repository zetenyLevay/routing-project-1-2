package routing.routingEngineDijkstra.dijkstra.model.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DijkstraFinalRoute {
    private final List<DijkstraRouteStep> routeSteps;
    private final double totalDistance;
    private final double totalTime;

    public DijkstraFinalRoute(List<DijkstraRouteStep> routeSteps, double totalDistance, double totalTime) {
        this.routeSteps = Collections.unmodifiableList(new ArrayList<>(routeSteps));
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    public List<DijkstraRouteStep> getRouteSteps() {
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