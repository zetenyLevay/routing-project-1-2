package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

public class RoadSegment {
    private String id;
    private int passengerFlow;
    private TransportType transportType;
    private Coordinate startCoordinates;
    private Coordinate endCoordinates;

    public RoadSegment(String id, int passengerFlow, TransportType transportType,
                        Coordinate startCoordinates, Coordinate endCoordinates) {
        this.id = id;
        this.passengerFlow = passengerFlow;
        this.transportType = transportType;
        this.startCoordinates = startCoordinates;
        this.endCoordinates = endCoordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPassengerFlow() {
        return passengerFlow;
    }

    public void setPassengerFlow(int passengerFlow) {
        this.passengerFlow = passengerFlow;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public Coordinate getStartCoordinates() {
        return startCoordinates;
    }
    public void setStartCoordinates(Coordinate startCoordinates) {
        this.startCoordinates = startCoordinates;
    }
    public Coordinate getEndCoordinates() {
        return endCoordinates;
    }
    public void setEndCoordinates(Coordinate endCoordinates) {
        this.endCoordinates = endCoordinates;
    }
}
