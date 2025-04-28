package routing.routingEngineModels;

import java.time.LocalTime;

public class InputJourney {
    Coordinate start;
    Coordinate end; 
    LocalTime startTime;

    public InputJourney(Coordinate start, Coordinate end, LocalTime startTime) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
    }


    public Coordinate getStart() {
        return this.start;
    }

    public Coordinate getEnd() {
        return this.end;
    }

    public LocalTime getStartTime() {
        return this.startTime;
    }
}


