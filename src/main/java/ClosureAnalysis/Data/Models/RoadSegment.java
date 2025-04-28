package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;
import Routing.routingenginemain.model.Coordinates;

import java.util.List;

public class RoadSegment {
    private final String id;
    private int passengerFlow;
    private TransportType transportType;
    private List<Stop> stopList;

    private final Coordinates startCoordinates;
    private final Coordinates endCoordinates;

    public RoadSegment(String id, Coordinates startCoordinates, Coordinates endCoordinates) {
        this.id = id;
        this.startCoordinates = startCoordinates;
        this.endCoordinates = endCoordinates;
    }

    public String getId() {
        return id;
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

    public List<Stop> getStopList() {
        return stopList;
    }
    public void setStopList(List<Stop> stopList) {
        this.stopList = stopList;
    }

    public Coordinates getEndCoordinates() {
        return endCoordinates;
    }

    public Coordinates getStartCoordinates() {
        return startCoordinates;
    }
}
