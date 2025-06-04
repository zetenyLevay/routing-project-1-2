package routing.routingEngineDijkstra.dijkstra.model.output;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraRouteInfo;

import java.util.Objects;

public class DijkstraRouteStep {
    private final String modeOfTransport;
    private final DijkstraCoordinates startCoord;
    private final DijkstraCoordinates endCoord;
    private final double time;
    private final String stopName;
    private final DijkstraRouteInfo routeInfo;

    public DijkstraRouteStep(String mode, DijkstraCoordinates start, DijkstraCoordinates end, double time) {
        this(mode, start, end, time, null, null);
    }

    public DijkstraRouteStep(String mode, DijkstraCoordinates start, DijkstraCoordinates end, double time, String stopName, DijkstraRouteInfo routeInfo) {
        this.modeOfTransport = Objects.requireNonNull(mode, "Mode of transport cannot be null");
        this.startCoord = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.endCoord = Objects.requireNonNull(end, "End coordinate cannot be null");
        if (time < 0) throw new IllegalArgumentException("Time cannot be negative");
        this.time = time;
        this.stopName = stopName;
        this.routeInfo = routeInfo;
    }

    public String getModeOfTransport() { return modeOfTransport; }
    public DijkstraCoordinates getStartCoord() { return startCoord; }
    public DijkstraCoordinates getEndCoord() { return endCoord; }
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
        DijkstraRouteStep routeStep = (DijkstraRouteStep) obj;
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
