package ClosureAnalysis.Data.Models;

import ClosureAnalysis.Data.Enums.TransportType;

import java.util.List;

public class Stop {
    private String id;
    private String name;
    private List<Double> coordinates;
    private TransportType transportType;
    private int routeAmount; /* how many routes go through the stop*/
    private int passengerCount;
    private int stopWorth; // Higher the better

    /* parameters could be changed to Builder */
    public Stop(String id, String name, List<Double> coordinates, int stopWorth,
                TransportType transportType, int passengerCount, int routeAmount) {
        this.id = id;
        this.name = name;
        this.transportType = transportType;
        this.passengerCount = passengerCount;
        this.routeAmount = routeAmount;
        this.stopWorth = stopWorth;
        this.coordinates = coordinates;
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

    public List<Double> getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public double getLatitude(){
        return coordinates.getFirst();
    }

    public double getLongitude(){
        return coordinates.getLast();
    }

    public int getStopWorth() {
        return stopWorth;
    }
    public void setStopWorth(int stopWorth) {
        this.stopWorth = stopWorth;
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
