package RoutingEngineDijkstra.Models;

import java.sql.Time;

public class InputJourney {
    Coordinate start;
    Coordinate end; 
    Time startTime;

    public InputJourney(Coordinate start, Coordinate end, Time startTime) {
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

    public Time getStartTime() {
        return this.startTime;
    }
}


