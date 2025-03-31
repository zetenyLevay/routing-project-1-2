package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

public class RoadSegment {
    private String id;
    private int passengerFlow;
    private TransportType transportType;
    private Stop startStop;
    private Stop endStop;

    public RoadSegment(String id, int passengerFlow, TransportType transportType,
                       Stop startStop, Stop endStop) {
        this.id = id;
        this.passengerFlow = passengerFlow;
        this.transportType = transportType;
        this.startStop = startStop;
        this.endStop = endStop;
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

    public Stop getStartStop() {
        return startStop;
    }

    public void setStartStop(Stop startStop) {
        this.startStop = startStop;
    }

    public Stop getEndStop() {
        return endStop;
    }

    public void setEndStop(Stop endStop) {
        this.endStop = endStop;
    }
}
