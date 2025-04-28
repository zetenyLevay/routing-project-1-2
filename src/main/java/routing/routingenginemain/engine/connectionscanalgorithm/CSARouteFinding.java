package routing.routingenginemain.engine.connectionscanalgorithm;

import routing.routingenginemain.engine.cache.classloader.ConnectionsCache;
import routing.routingenginemain.engine.cache.classloader.StopsCache;
import routing.routingenginemain.model.CSAAPImodel.CSAQuery;
import routing.routingenginemain.model.CSAAPImodel.ResultantRouteCSA;
import routing.routingenginemain.model.CSAAPImodel.RouteSegmentCSA;
import routing.routingenginemain.model.Connection;
import routing.routingenginemain.model.Stop;
import routing.routingenginemain.model.pathway.Pathway;

import java.util.*;

public class  CSARouteFinding {
    private final Map<Stop, Integer> earliestArrival;
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
        StopsCache.getAllStops().forEach(stop -> {
            earliestArrival.put(stop, Integer.MAX_VALUE);
            parentConnection.put(stop, null);
            parentFootpath.put(stop, null);
        });
        earliestArrival.put(csaQuery.getDepartureStop(), csaQuery.translateTime(csaQuery.getDepartureTime()));
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
        int departureTime = csaQuery.translateTime(csaQuery.getDepartureTime());

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int depTime = sortedConnections.get(mid).getDepTime();

            if (depTime >= departureTime) {
                result = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    private boolean shouldTerminateSearch(Connection conn) {
        return earliestArrival.get(csaQuery.getArrivalStop()) <= conn.getDepTime();
    }

    private boolean canBoardAtStop(Connection conn) {
        return conn.getDepTime() >= earliestArrival.get(conn.getDepStop());
    }

    private boolean improvesArrivalTime(Connection conn) {
        return conn.getArrTime() < earliestArrival.get(conn.getArrStop());
    }

    private void updateConnection(Connection conn) {
        earliestArrival.put(conn.getArrStop(), conn.getArrTime());
        parentConnection.put(conn.getArrStop(), conn);
        parentFootpath.put(conn.getArrStop(), null);
    }

    private void processTransfers(Connection conn) {
        for (Pathway pathway : conn.getArrStop().getFootpaths()) {
            int newArrival = conn.getArrTime() + pathway.getTraversalTime();
            if (newArrival < earliestArrival.get(pathway.getToStop())) {
                earliestArrival.put(pathway.getToStop(), newArrival);
                parentConnection.put(pathway.getToStop(), conn);
                parentFootpath.put(pathway.getToStop(), pathway);
            }
        }
    }

    private ResultantRouteCSA reconstructRoute() {
        if (earliestArrival.get(csaQuery.getArrivalStop()) == Integer.MAX_VALUE) {
            return ResultantRouteCSA.notFound();
        }

        List<RouteSegmentCSA> segments = new ArrayList<>();
        Stop current = csaQuery.getArrivalStop();

        while (!current.equals(csaQuery.getDepartureStop())) {
            segments.add(buildSegment(current));
            current = getPreviousStop(current);
        }

        Collections.reverse(segments);
        return ResultantRouteCSA.create(earliestArrival.get(csaQuery.getArrivalStop()), segments);
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