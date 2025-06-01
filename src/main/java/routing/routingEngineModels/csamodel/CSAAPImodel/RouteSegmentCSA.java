package routing.routingEngineModels.csamodel.CSAAPImodel;

import routing.routingEngineModels.Connection;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.csamodel.pathway.Pathway;

import java.time.LocalTime;
import java.time.Duration;

public class RouteSegmentCSA {
    private final RouteSegmentTypeCSA routeSegmentTypeCSA;
    private final Connection connection;
    private final Pathway pathway;
    private final LocalTime startTime;
    private final Duration duration;

    private RouteSegmentCSA(RouteSegmentTypeCSA routeSegmentTypeCSA, Connection connection, Pathway pathway) {
        this.routeSegmentTypeCSA = routeSegmentTypeCSA;
        this.connection = connection;
        this.pathway = pathway;

        if (routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT) {
            this.startTime = connection.getDepTime();
            this.duration = Duration.between(connection.getDepTime(), connection.getArrTime());
        } else {
            this.startTime = connection.getArrTime();
            this.duration = Duration.ofSeconds(pathway.getTraversalTime());
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Stop getFromStop() {
        return routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT ?
                connection.getDepStop() :
                pathway.getFromStop();
    }

    public Stop getToStop() {
        return routeSegmentTypeCSA == RouteSegmentTypeCSA.TRANSIT ?
                connection.getArrStop() :
                pathway.getToStop();
    }
}
