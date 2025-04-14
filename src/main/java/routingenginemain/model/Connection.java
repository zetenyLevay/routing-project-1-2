package routingenginemain.model;

public class Connection {
    private final Trip trip;
    private final Stop depSTop;
    private final Stop arrStop;
    private final int depTime;
    private final int arrTime;

    public Connection(Trip trip, Stop depSTop, Stop arrStop, int depTime, int arrTime) {
        this.trip = trip;
        this.depSTop = depSTop;
        this.arrStop = arrStop;
        this.depTime = depTime;
        this.arrTime = arrTime;
    }

    public Trip getTrip() {
        return trip;
    }

    public Stop getDepSTop() {
        return depSTop;
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
