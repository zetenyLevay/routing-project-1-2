package routing.routingEngineModels;

public class RouteStep {

    // {Mode of transpotr, startCoord, toCoord, numOfMinutes}
    private String modeOfTransport;
    // private Coordinates startCoord;
    private Coordinates toCoord;
    private double numOfMinutes;
    private String startTime;
    private String stopStr;

    public RouteStep(String modeOfTransport, Coordinates toCoord, double numOfMinutes, String startTime, String stopStr) {
        this.modeOfTransport = modeOfTransport;
        this.toCoord = toCoord;
        this.numOfMinutes = numOfMinutes;
        this.startTime = startTime;
        this.stopStr = stopStr;
    }

    //for walking steps 
    public RouteStep(String modeOfTransport, Coordinates toCoord, String startTime) {
        this.modeOfTransport = modeOfTransport;
        this.toCoord = toCoord;
        this.startTime = startTime;
    }

    public String getModeOfTransport() {
        return modeOfTransport;
    }

    public Coordinates getToCoord() {
        return toCoord;
    }

    public double getNumOfMinutes() {
        return numOfMinutes;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStopStr() {
        return stopStr;
    }

}
