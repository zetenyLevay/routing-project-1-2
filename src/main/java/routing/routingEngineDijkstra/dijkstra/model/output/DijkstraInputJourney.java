package routing.routingEngineDijkstra.dijkstra.model.output;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import java.time.LocalTime;
import java.util.Objects;

public class DijkstraInputJourney {
    private final DijkstraCoordinates start;
    private final DijkstraCoordinates end;
    private final LocalTime startTime;

    public DijkstraInputJourney(DijkstraCoordinates start, DijkstraCoordinates end, LocalTime startTime) {
        this.start = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.end = Objects.requireNonNull(end, "End coordinate cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
    }

    public DijkstraCoordinates getStart() { return start; }
    public DijkstraCoordinates getEnd() { return end; }
    public LocalTime getStartTime() { return startTime; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DijkstraInputJourney that = (DijkstraInputJourney) obj;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, startTime);
    }
}