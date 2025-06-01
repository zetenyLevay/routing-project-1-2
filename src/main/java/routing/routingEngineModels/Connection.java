package routing.routingEngineModels;

import routing.routingEngineCSA.engine.util.TimeConverter;
import routing.routingEngineModels.Stop.Stop;
import java.time.LocalTime;

public class Connection {
    private final Trip trip;
    private final Stop depStop;
    private final Stop arrStop;
    private final LocalTime depTime;
    private final LocalTime arrTime;

    public Connection(Trip trip, Stop depStop, Stop arrStop, LocalTime depTime, LocalTime arrTime) {
        this.trip = trip;
        this.depStop = depStop;
        this.arrStop = arrStop;
        this.depTime = depTime;
        this.arrTime = arrTime;

        if (depTime == null || arrTime == null) {
            throw new IllegalArgumentException("Departure and arrival times cannot be null");
        }
    }

    public Trip getTrip() {
        return trip;
    }

    public Stop getDepStop() {
        return depStop;
    }

    public Stop getArrStop() {
        return arrStop;
    }

    public LocalTime getArrTime() {
        return arrTime;
    }

    public LocalTime getDepTime() {
        return depTime;
    }

    @Override
    public String toString() {
        return String.format("%s [%s → %s]", trip.getTripID(),
                TimeConverter.formatAsGTFSTime(depTime),
                TimeConverter.formatAsGTFSTime(arrTime));
    }
}