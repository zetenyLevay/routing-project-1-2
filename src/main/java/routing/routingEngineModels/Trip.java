package routing.routingEngineModels;

import routing.routingEngineModels.csamodel.route.Route;

public class Trip {
    private final String tripID;
    private final Route route;
//    private final List<Connection> connections;
    private final String headSign;

    public Trip(String tripID, Route route, String headSign) {
        this.tripID = tripID;
        this.route = route;
//        this.connections = connections;
        this.headSign = headSign;
    }

    public String getTripID() {
        return tripID;
    }

    public Route getRoute() {
        return route;
    }

//    public List<Connection> getConnections() {
//        return connections;
//    }

    public String getHeadSign() {
        return headSign;
    }
}
