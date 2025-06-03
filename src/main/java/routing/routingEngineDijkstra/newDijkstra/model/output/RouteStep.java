package routing.routingEngineDijkstra.newDijkstra.model.output;

import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraRouteInfo;

import java.util.Objects;

public class RouteStep {
    private final String modeOfTransport;
    private final Coordinates startCoord;
    private final Coordinates endCoord;
    private final double time;
    private final String stopName;
    private final DijkstraRouteInfo routeInfo;

    public RouteStep(String mode, Coordinates start, Coordinates end, double time) {
        this(mode, start, end, time, null, null);
    }

    public RouteStep(String mode, Coordinates start, Coordinates end, double time, String stopName, DijkstraRouteInfo routeInfo) {
        this.modeOfTransport = Objects.requireNonNull(mode, "Mode of transport cannot be null");
        this.startCoord = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.endCoord = Objects.requireNonNull(end, "End coordinate cannot be null");
        if (time < 0) throw new IllegalArgumentException("Time cannot be negative");
        this.time = time;
        this.stopName = stopName;
        this.routeInfo = routeInfo;
    }

    public String getModeOfTransport() { return modeOfTransport; }
    public Coordinates getStartCoord() { return startCoord; }
    public Coordinates getEndCoord() { return endCoord; }
    public double getTime() { return time; }
    public String getStopName() { return stopName; }
    public DijkstraRouteInfo getRouteInfo() { return routeInfo; }

    public boolean isWalkingStep() {
        return "WALK".equalsIgnoreCase(modeOfTransport);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RouteStep routeStep = (RouteStep) obj;
        return Double.compare(routeStep.time, time) == 0 &&
                Objects.equals(modeOfTransport, routeStep.modeOfTransport) &&
                Objects.equals(startCoord, routeStep.startCoord) &&
                Objects.equals(endCoord, routeStep.endCoord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modeOfTransport, startCoord, endCoord, time);
    }
}
