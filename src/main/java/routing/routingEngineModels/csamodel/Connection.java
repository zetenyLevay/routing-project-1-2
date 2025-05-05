package routing.routingEngineModels.csamodel;

public class Connection {
    private final Trip trip;
    private final Stop depStop;
    private final Stop arrStop;
    private final int depTime;
    private final int arrTime;

    public Connection(Trip trip, Stop depSTop, Stop arrStop, int depTime, int arrTime) {
        this.trip = trip;
        this.depStop = depSTop;
        this.arrStop = arrStop;
        this.depTime = depTime;
        this.arrTime = arrTime;
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

    public int getArrTime() {
        return arrTime;
    }

    public int getDepTime() {
        return depTime;
    }
}
