package routing.routingEngineDijkstra.newDijkstra.model.output;

import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;
import java.time.LocalTime;
import java.util.Objects;

public class InputJourney {
    private final Coordinates start;
    private final Coordinates end;
    private final LocalTime startTime;

    public InputJourney(Coordinates start, Coordinates end, LocalTime startTime) {
        this.start = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.end = Objects.requireNonNull(end, "End coordinate cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
    }

    public Coordinates getStart() { return start; }
    public Coordinates getEnd() { return end; }
    public LocalTime getStartTime() { return startTime; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InputJourney that = (InputJourney) obj;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, startTime);
    }
}