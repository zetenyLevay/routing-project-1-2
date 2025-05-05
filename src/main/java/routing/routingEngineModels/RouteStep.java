package routing.routingEngineModels;

public class RouteStep {
    // {Mode of transpotr, startCoord, endCoord, Time}
    private String modeOfTransport;
    private Coordinates startCoord;
    private Coordinates endCoord;
    private double time;

    public RouteStep(String modeOfTransport, Coordinates startCoord, Coordinates endCoord, double time) {
        this.modeOfTransport = modeOfTransport;
        this.startCoord = startCoord;
        this.endCoord = endCoord;
        this.time = time;
    }
    public String getModeOfTransport() {
        return modeOfTransport;
    }
    public Coordinates getStartCoord() {
        return startCoord;
    }   
    public Coordinates getEndCoord() {
        return endCoord;
    }
    public double getTime() {
        return time;
    }
    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }
}
