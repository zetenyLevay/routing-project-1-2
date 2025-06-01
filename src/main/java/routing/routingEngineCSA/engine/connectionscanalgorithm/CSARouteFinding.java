package routing.routingEngineCSA.engine.connectionscanalgorithm;

import routing.routingEngineCSA.engine.cache.classloader.ConnectionsCache;
import routing.routingEngineCSA.engine.cache.classloader.StopsCache;
import routing.routingEngineCSA.engine.util.TimeConverter;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.CSAAPImodel.ResultantRouteCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentCSA;
import routing.routingEngineModels.Connection;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.csamodel.pathway.Pathway;

import java.time.LocalTime;
import java.util.*;

public class CSARouteFinding {
    private final Map<Stop, LocalTime> earliestArrival;
    private final Map<Stop, Connection> parentConnection;
    private final Map<Stop, Pathway> parentFootpath;
    private final List<Connection> sortedConnections;
    private final CSAQuery csaQuery;

    public CSARouteFinding(CSAQuery csaQuery) {
        this.earliestArrival = new HashMap<>();
        this.parentConnection = new HashMap<>();
        this.parentFootpath = new HashMap<>();
        this.sortedConnections = ConnectionsCache.getSortedConnections();
        this.csaQuery = csaQuery;
    }

    public ResultantRouteCSA findRouteViaCSA() {
        initialize();
        processConnections();
        return reconstructRoute();
    }

    private void initialize() {
        LocalTime maxTime = LocalTime.MAX;
        StopsCache.getAllStops().forEach(stop -> {
            earliestArrival.put(stop, maxTime);
            parentConnection.put(stop, null);
            parentFootpath.put(stop, null);
        });
        earliestArrival.put(csaQuery.getDepartureStop(), csaQuery.getDepartureTime());
    }

    private void processConnections() {
        int firstRelevant = findFirstRelevantConnectionIndex();

        for (int i = firstRelevant; i < sortedConnections.size(); i++) {
            Connection conn = sortedConnections.get(i);
            if (shouldTerminateSearch(conn)) break;
            if (canBoardAtStop(conn) && improvesArrivalTime(conn)) {
                updateConnection(conn);
                processTransfers(conn);
            }
        }
    }

    private int findFirstRelevantConnectionIndex() {
        int low = 0;
        int high = sortedConnections.size() - 1;
        int result = sortedConnections.size();
        LocalTime departureTime = csaQuery.getDepartureTime();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            LocalTime depTime = sortedConnections.get(mid).getDepTime();

            if (TimeConverter.isBeforeOrEqual(departureTime, depTime)) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    private boolean shouldTerminateSearch(Connection conn) {
        return TimeConverter.isBeforeOrEqual(
                earliestArrival.get(csaQuery.getArrivalStop()),
                conn.getDepTime()
        );
    }

    private boolean canBoardAtStop(Connection conn) {
        return TimeConverter.isBeforeOrEqual(
                earliestArrival.get(conn.getDepStop()),
                conn.getDepTime()
        );
    }

    private boolean improvesArrivalTime(Connection conn) {
        return conn.getArrTime().isBefore(earliestArrival.get(conn.getArrStop()));
    }

    private void updateConnection(Connection conn) {
        earliestArrival.put(conn.getArrStop(), conn.getArrTime());
        parentConnection.put(conn.getArrStop(), conn);
        parentFootpath.put(conn.getArrStop(), null);
    }

    private void processTransfers(Connection conn) {
        for (Pathway pathway : conn.getArrStop().getFootpaths()) {
            LocalTime newArrival = conn.getArrTime().plusSeconds(pathway.getTraversalTime());
            if (newArrival.isBefore(earliestArrival.get(pathway.getToStop()))) {
                earliestArrival.put(pathway.getToStop(), newArrival);
                parentConnection.put(pathway.getToStop(), conn);
                parentFootpath.put(pathway.getToStop(), pathway);
            }
        }
    }

    private ResultantRouteCSA reconstructRoute() {
        if (earliestArrival.get(csaQuery.getArrivalStop()).equals(LocalTime.MAX)) {
            return ResultantRouteCSA.notFound();
        }

        List<RouteSegmentCSA> segments = new ArrayList<>();
        Stop current = csaQuery.getArrivalStop();

        while (!current.equals(csaQuery.getDepartureStop())) {
            segments.add(buildSegment(current));
            current = getPreviousStop(current);
        }

        Collections.reverse(segments);
        return ResultantRouteCSA.create(
                earliestArrival.get(csaQuery.getArrivalStop()),
                segments
        );
    }

    private RouteSegmentCSA buildSegment(Stop current) {
        Pathway pathway = parentFootpath.get(current);
        if (pathway == null) {
            Connection conn = parentConnection.get(current);
            return RouteSegmentCSA.forTransit(conn);
        } else {
            Connection precedingConn = parentConnection.get(current);
            return RouteSegmentCSA.forTransfer(pathway, precedingConn);
        }
    }

    private Stop getPreviousStop(Stop current) {
        Pathway pathway = parentFootpath.get(current);
        return pathway != null ? pathway.getFromStop() : parentConnection.get(current).getDepStop();
    }
}
