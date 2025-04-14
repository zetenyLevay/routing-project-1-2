package routingenginemain.engine;

import routingenginemain.model.Coordinates;

import java.time.LocalTime;


public class Request
{
    private final Coordinates departFrom;
    private final Coordinates arriveAt;
    private final int time;

    public Request(Coordinates start, Coordinates end, LocalTime time) {
        this.time = time.toSecondOfDay();
        this.departFrom = start;
        this.arriveAt = end;
    }

    public int getDepartureTime() {
        return this.time;
    }

    public Coordinates getDepartureCoordinates() {
        return this.departFrom;
    }

    public Coordinates getArrivalCoordinates() {
        return this.arriveAt;
    }



}
