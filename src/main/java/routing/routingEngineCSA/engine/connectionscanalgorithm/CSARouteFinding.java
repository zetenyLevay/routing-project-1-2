package routing.routingEngineCSA.engine.connectionscanalgorithm;

import routing.routingEngineCSA.engine.cache.classloader.ConnectionsCache;
import routing.routingEngineCSA.engine.cache.classloader.StopsCache;
import routing.routingEngineCSA.engine.util.TimeConverter;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.CSAAPImodel.ResultantRouteCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentCSA;
import routing.routingEngineModels.Connection;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;
import java.util.*;

public class CSARouteFinding {
    private final Map<Stop, LocalTime> earliestArrival;
    private final Map<Stop, Connection> parentConnection;
    private final List<Connection> sortedConnections;
    private final CSAQuery csaQuery;

    public CSARouteFinding(CSAQuery csaQuery) {
        this.earliestArrival = new HashMap<>();
        this.parentConnection = new HashMap<>();
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
        for (Stop stop : StopsCache.getAllStops()) {
            earliestArrival.put(stop, maxTime);
            parentConnection.put(stop, null);
        }
        earliestArrival.put(csaQuery.getDepartureStop(), csaQuery.getDepartureTime());
    }

    private void processConnections() {
        int firstRelevant = findFirstRelevantConnectionIndex();

        for (int i = firstRelevant; i < sortedConnections.size(); i++) {
            Connection conn = sortedConnections.get(i);
            if (shouldTerminateSearch(conn)) break;
            if (canBoardAtStop(conn) && improvesArrivalTime(conn)) {
                updateConnection(conn);
                updateTransferReachability(conn); // All transfers assumed instantaneous
            }
        }
    }

    private int findFirstRelevantConnectionIndex() {
        int low = 0, high = sortedConnections.size() - 1, result = sortedConnections.size();
        LocalTime departureTime = csaQuery.getDepartureTime();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (TimeConverter.isBeforeOrEqual(departureTime, sortedConnections.get(mid).getDepTime())) {
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
                earliestArrival.get(csaQuery.getArrivalStop()), conn.getDepTime()
        );
    }

    private boolean canBoardAtStop(Connection conn) {
        return TimeConverter.isBeforeOrEqual(
                earliestArrival.get(conn.getDepStop()), conn.getDepTime()
        );
    }

    private boolean improvesArrivalTime(Connection conn) {
        return conn.getArrTime().isBefore(earliestArrival.get(conn.getArrStop()));
    }

    private void updateConnection(Connection conn) {
        earliestArrival.put(conn.getArrStop(), conn.getArrTime());
        parentConnection.put(conn.getArrStop(), conn);
    }

    // Update reachable stops due to instant transfers
    private void updateTransferReachability(Connection conn) {
        LocalTime arrivalTime = conn.getArrTime();
        for (Stop stop : StopsCache.getAllStops()) {
            if (arrivalTime.isBefore(earliestArrival.get(stop))) {
                earliestArrival.put(stop, arrivalTime);
                parentConnection.put(stop, conn);
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
            Connection conn = parentConnection.get(current);
            segments.add(RouteSegmentCSA.forTransit(conn));
            current = conn.getDepStop();
        }

        Collections.reverse(segments);
        return ResultantRouteCSA.create(
                earliestArrival.get(csaQuery.getArrivalStop()), segments
        );
    }
}
