package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

import java.util.List;

public class RoadSegment {
    private String id;
    private int passengerFlow;
    private TransportType transportType;
    private List<Stop> stopList;

    public RoadSegment(String id, int passengerFlow, TransportType transportType, List<Stop> stopList) {
        this.id = id;
        this.passengerFlow = passengerFlow;
        this.transportType = transportType;
        this.stopList = stopList;
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

    public List<Stop> getStopList() {
        return stopList;
    }
    public void setStopList(List<Stop> stopList) {
        this.stopList = stopList;
    }

}
