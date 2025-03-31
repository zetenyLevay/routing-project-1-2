package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

public class Stop {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private TransportType transportType;

    private int routeAmount; /* how many routes go through the stop*/
    private int passengerCount;

    public Stop(String id, String name, double latitude, double longitude,
                TransportType transportType, int passengerCount, int routeAmount) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.transportType = transportType;
        this.passengerCount = passengerCount;
        this.routeAmount = routeAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }
    public int getRouteAmount() {
        return routeAmount;
    }
    public void setRouteAmount(int routeAmount) {
        this.routeAmount = routeAmount;
    }
}
