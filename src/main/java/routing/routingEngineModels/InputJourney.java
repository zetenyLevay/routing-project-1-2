package routing.routingEngineModels;

import java.time.LocalTime;

public class InputJourney {
    Coordinates start;
    Coordinates end;
    LocalTime startTime;

    public InputJourney(Coordinates start, Coordinates end, LocalTime startTime) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
    }


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


