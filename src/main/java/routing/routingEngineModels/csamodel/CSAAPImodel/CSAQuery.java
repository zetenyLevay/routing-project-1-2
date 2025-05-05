package routing.routingEngineModels.csamodel.CSAAPImodel;

import routing.routingEngineModels.csamodel.Stop;

import java.time.LocalTime;

public class CSAQuery {
    private final Stop departureStop;
    private final Stop arrivalStop;
    private final LocalTime departureTime;

    public CSAQuery(Stop departureStop, Stop arrivalStop, LocalTime departureTime) {
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
        this.departureTime = departureTime;
    }

    public int translateTime(LocalTime time) {
        return time.toSecondOfDay();
    }


    public Stop getDepartureStop() {
        return departureStop;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public Stop getArrivalStop() {
        return arrivalStop;
    }
}
