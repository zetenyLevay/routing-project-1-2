package Routing.routingenginemain.model.CSAAPImodel;

import Routing.routingenginemain.model.Connection;
import Routing.routingenginemain.model.Stop;
import Routing.routingenginemain.model.pathway.Pathway;

public class RouteSegmentCSA {
    private final RouteSegmentTypeCSA routeSegmentTypeCSA;
    private final Connection connection;
    private final Pathway pathway;
    private final int startTime;
    private final int duration;

    private RouteSegmentCSA(RouteSegmentTypeCSA routeSegmentTypeCSA, Connection connection, Pathway pathway) {
        this.routeSegmentTypeCSA = routeSegmentTypeCSA;
        this.connection = connection;
        this.pathway = pathway;

        if (routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT) {
            this.startTime = connection.getDepTime();
            this.duration = connection.getArrTime() - connection.getDepTime();
        } else {
            this.startTime = connection.getArrTime();
            this.duration = pathway.getTraversalTime();

        }
    }

    public static RouteSegmentCSA forTransit(Connection connection) {
        return new RouteSegmentCSA(RouteSegmentTypeCSA.TRANSIT, connection, null);
    }

    public static RouteSegmentCSA forTransfer(Pathway pathway, Connection precedingConnection) {
        return new RouteSegmentCSA(RouteSegmentTypeCSA.TRANSFER, precedingConnection, pathway);
    }


    public RouteSegmentTypeCSA getRouteSegmentTypeCSA() {
        return routeSegmentTypeCSA;
    }


    public Connection getConnection() {
        return connection;
    }

    public Pathway getPathway() {
        return pathway;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public Stop getFromStop() {
        return this.routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT ?
                connection.getDepStop() :
                pathway.getFromStop();
    }

    public Stop getToStop() {
        return this.routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT ?
                connection.getArrStop() :
                pathway.getToStop();
    }
}
