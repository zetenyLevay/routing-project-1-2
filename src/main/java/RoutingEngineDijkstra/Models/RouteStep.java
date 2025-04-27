package RoutingEngineDijkstra.Models;

public class RouteStep {
    // {Mode of transpotr, startCoord, endCoord, Time}
    private String modeOfTransport;
    private Coordinate startCoord;
    private Coordinate endCoord;
    private double time;

    public RouteStep(String modeOfTransport, Coordinate startCoord, Coordinate endCoord, double time) {
        this.modeOfTransport = modeOfTransport;
        this.startCoord = startCoord;
        this.endCoord = endCoord;
        this.time = time;
    }
    public String getModeOfTransport() {
        return modeOfTransport;
    }
    public Coordinate getStartCoord() {
        return startCoord;
    }   
    public Coordinate getEndCoord() {
        return endCoord;
    }
    public double getTime() {
        return time;
    }
    public void setModeOfTransport(String modeOfTransport) {
        this.modeOfTransport = modeOfTransport;
    }
}
