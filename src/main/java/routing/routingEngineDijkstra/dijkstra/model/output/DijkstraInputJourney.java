package routing.routingEngineDijkstra.dijkstra.model.output;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents the input for a journey, including start and end coordinates and start time.
 */
public class DijkstraInputJourney {
    private final DijkstraCoordinates start;
    private final DijkstraCoordinates end;
    private final LocalTime startTime;

    /**
     * Constructs a DijkstraInputJourney with the specified start, end, and start time.
     *
     * @param start      the starting coordinates
     * @param end        the destination coordinates
     * @param startTime  the start time of the journey
     * @throws NullPointerException if any parameter is null
     */
    public DijkstraInputJourney(DijkstraCoordinates start, DijkstraCoordinates end, LocalTime startTime) {
        this.start = Objects.requireNonNull(start, "Start coordinate cannot be null");
        this.end = Objects.requireNonNull(end, "End coordinate cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cannot be null");
    }

    /**
     * Retrieves the starting coordinates of the journey.
     *
     * @return the start DijkstraCoordinates
     */
    public DijkstraCoordinates getStart() {
        return start;
    }

    /**
     * Retrieves the destination coordinates of the journey.
     *
     * @return the end DijkstraCoordinates
     */
    public DijkstraCoordinates getEnd() {
        return end;
    }

    /**
     * Retrieves the start time of the journey.
     *
     * @return the start time as a LocalTime
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Checks if this journey is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DijkstraInputJourney that = (DijkstraInputJourney) obj;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(startTime, that.startTime);
    }

    /**
     * Generates a hash code for this journey.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(start, end, startTime);
    }
}