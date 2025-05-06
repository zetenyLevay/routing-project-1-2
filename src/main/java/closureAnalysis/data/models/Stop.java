package closureAnalysis.data.models;


import closureAnalysis.data.enums.TransportType;
import routing.routingEngineModels.csamodel.Coordinates;

public class Stop{
    private final String id;
    private final String name;
    private final Coordinates coordinates;
    private TransportType transportType;
    private int routeAmount; /* how many routes go through the stop*/
    private int passengerCount;

    private NearbyPOIs nearbyPOIs;
    private int stopWorth; // Higher the better

    /* parameters could be changed to Builder */
    public Stop(String id, String name, Coordinates coordinates) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
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

    public void reset() {
        this.stopWorth = 0;
        this.transportType = null;
        this.nearbyPOIs = null;
        this.passengerCount = 0;
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
    public NearbyPOIs getNearbyPOIs() {return nearbyPOIs;}
    public void setNearbyPOIs(NearbyPOIs nearbyPOIs) {this.nearbyPOIs = nearbyPOIs;}
}
