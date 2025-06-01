package routing.routingEngineModels.csamodel.MainAPImodel;

import routing.routingEngineModels.Coordinates;

import java.time.LocalTime;


public class Request
{
    private final Coordinates departFrom;
    private final Coordinates arriveAt;
    private final LocalTime time;

    public Request(Coordinates start, Coordinates end, LocalTime time) {
        this.time = time;
        this.departFrom = start;
        this.arriveAt = end;
    }

    public LocalTime getDepartureTime() {
        return this.time;
    }

    public Coordinates getDepartureCoordinates() {
        return this.departFrom;
    }

    public Coordinates getArrivalCoordinates() {
        return this.arriveAt;
    }



}
