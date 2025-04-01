package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

public class RoadSegment {
    private String id;
    private int passengerFlow;
    private TransportType transportType;


    public RoadSegment(String id, int passengerFlow, TransportType transportType
                       ) {
        this.id = id;
        this.passengerFlow = passengerFlow;
        this.transportType = transportType;

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


}
