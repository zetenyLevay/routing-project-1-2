package routing.routingEngineModels;

import java.time.LocalTime;

/**
 * InputJourney.java
 *
 * Represents a journey with a start and end location, and a start time.
 * This class is used to encapsulate the details of a journey for routing purposes.
 */
public class InputJourney {
    Coordinates start;
    Coordinates end;
    LocalTime startTime;

    /**
     * Constructor for InputJourney.
     *
     * @param start The starting coordinates of the journey.
     * @param end The ending coordinates of the journey.
     * @param startTime The time when the journey starts.
     */
    public InputJourney(Coordinates start, Coordinates end, LocalTime startTime) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
    }


    // Getters
    public Coordinates getStart() {
        return this.start;
    }

    public Coordinates getEnd() {
        return this.end;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }
}


