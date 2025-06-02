package routing.routingEngineCSA.engine.connectionscanalgorithm;

import routing.routingEngineCSA.engine.cache.classloader.ConnectionsCache;
import routing.routingEngineCSA.engine.cache.classloader.StopsCache;
// NO import routing.routingEngineModels.csamodel.pathway.Pathway; // COMPLETELY REMOVED
import routing.routingEngineCSA.engine.util.TimeConverter;
import routing.routingEngineCSA.engine.util.StraightLineCalculator;
import routing.routingEngineModels.csamodel.CSAAPImodel.CSAQuery;
import routing.routingEngineModels.csamodel.CSAAPImodel.ResultantRouteCSA;
import routing.routingEngineModels.csamodel.CSAAPImodel.RouteSegmentCSA;
import routing.routingEngineModels.Connection;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.Coordinates;

import java.time.LocalTime;
import java.util.*;

public class CSARouteFinding {

    private static class TransferDetails {
        final Stop fromStop;
        final Stop toStop;
        final int traversalTimeSeconds;
        final String type;

        TransferDetails(Stop fromStop, Stop toStop, int traversalTimeSeconds, String type) {
            this.fromStop = fromStop;
            this.toStop = toStop;
            this.traversalTimeSeconds = traversalTimeSeconds;
            this.type = type;
        }

        public Stop getFromStop() { return fromStop; }
        public Stop getToStop() { return toStop; }
        public int getTraversalTimeSeconds() { return traversalTimeSeconds; }
        public String getType() { return type; }
    }

    private final Map<Stop, LocalTime> earliestArrival;
    private final Map<Stop, Connection> parentConnection;
    private final Map<Stop, TransferDetails> parentTransfer;
    private final List<Connection> sortedConnections;
    private final CSAQuery csaQuery;
    private LocalTime bestArrivalTime;
    private static final int MIN_TRANSFER_TIME_SECONDS = 120;   // Minimum transfer time (2 minutes)
    private static final int MAX_WALKING_TIME_SECONDS = 600;    // Maximum walking time (10 minutes)

    public CSARouteFinding(CSAQuery csaQuery) {
        this.earliestArrival = new HashMap<>();
        this.parentConnection = new HashMap<>();
        this.parentTransfer = new HashMap<>();
        this.sortedConnections = ConnectionsCache.getSortedConnections();
        this.csaQuery = csaQuery;
        this.bestArrivalTime = LocalTime.MAX;
    }

    public ResultantRouteCSA findRouteViaCSA() {
        initialize();
        processConnections();
        return reconstructRoute();
    }

    private void initialize() {
        StopsCache.getAllStops().forEach(stop -> {
            earliestArrival.put(stop, LocalTime.MAX);
            parentConnection.put(stop, null);
            parentTransfer.put(stop, null);
        });
        earliestArrival.put(csaQuery.getDepartureStop(), csaQuery.getDepartureTime());
    }

    private void processConnections() {
        int firstRelevant = findFirstRelevantConnectionIndex();

        for (int i = firstRelevant; i < sortedConnections.size(); i++) {
            Connection conn = sortedConnections.get(i);

            if (conn.getDepTime().isAfter(bestArrivalTime) && !bestArrivalTime.equals(LocalTime.MAX)) {
                break;
            }

            if (canBoardAtStop(conn) && improvesArrivalTime(conn)) {
                updateAfterDirectConnection(conn);

                if (conn.getArrStop().equals(csaQuery.getArrivalStop())) {
                    bestArrivalTime = conn.getArrTime();
                }
                processCalculatedTransfers(conn);
            }
        }
    }

    private void processCalculatedTransfers(Connection connectionToTransferStartStop) {
        Stop fromStop = connectionToTransferStartStop.getArrStop();
        LocalTime arrivalTimeAtFromStop = connectionToTransferStartStop.getArrTime();

        for (Stop toStop : StopsCache.getAllStops()) {
            if (toStop.equals(fromStop)) {
                continue;
            }

            Coordinates toStopCoordinates = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
            int walkingTimeSeconds = StraightLineCalculator.calculateWalkingTimeSeconds(fromStop, toStopCoordinates);

            if (walkingTimeSeconds > MAX_WALKING_TIME_SECONDS) {
                continue;
            }

            int actualTransferDurationSeconds = Math.max(walkingTimeSeconds, MIN_TRANSFER_TIME_SECONDS);
            LocalTime arrivalTimeAtToStopViaTransfer = arrivalTimeAtFromStop.plusSeconds(actualTransferDurationSeconds);

            if (arrivalTimeAtToStopViaTransfer.isBefore(earliestArrival.get(toStop))) {
                earliestArrival.put(toStop, arrivalTimeAtToStopViaTransfer);

                parentConnection.put(toStop, connectionToTransferStartStop);

                TransferDetails transfer = new TransferDetails(fromStop, toStop, actualTransferDurationSeconds, "WALKING");
                parentTransfer.put(toStop, transfer);

                if (toStop.equals(csaQuery.getArrivalStop())) {
                    bestArrivalTime = arrivalTimeAtToStopViaTransfer;
                }
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

    private boolean canBoardAtStop(Connection conn) {
        return TimeConverter.isBeforeOrEqual(
                earliestArrival.get(conn.getDepStop()),
                conn.getDepTime()
        );
    }

    private boolean improvesArrivalTime(Connection conn) {
        return conn.getArrTime().isBefore(earliestArrival.get(conn.getArrStop()));
    }

    private void updateAfterDirectConnection(Connection conn) {
        earliestArrival.put(conn.getArrStop(), conn.getArrTime());
        parentConnection.put(conn.getArrStop(), conn);
        parentTransfer.put(conn.getArrStop(), null);
    }

    private ResultantRouteCSA reconstructRoute() {
        if (earliestArrival.get(csaQuery.getArrivalStop()).equals(LocalTime.MAX)) {
            return ResultantRouteCSA.notFound();
        }

        List<RouteSegmentCSA> segments = new ArrayList<>();
        Stop currentIterStop = csaQuery.getArrivalStop();

        while (!currentIterStop.equals(csaQuery.getDepartureStop())) {
            TransferDetails transferTakenToCurrentIterStop = parentTransfer.get(currentIterStop);
            Connection connectionAssociatedWithCurrentIterStop = parentConnection.get(currentIterStop);

            if (transferTakenToCurrentIterStop != null) {
                segments.add(RouteSegmentCSA.forWalkingTransfer(
                        transferTakenToCurrentIterStop.getFromStop(),
                        transferTakenToCurrentIterStop.getToStop(),
                        transferTakenToCurrentIterStop.getTraversalTimeSeconds(),
                        transferTakenToCurrentIterStop.getType(),
                        connectionAssociatedWithCurrentIterStop
                ));
                currentIterStop = transferTakenToCurrentIterStop.getFromStop();
            } else if (connectionAssociatedWithCurrentIterStop != null) {
                segments.add(RouteSegmentCSA.forTransit(connectionAssociatedWithCurrentIterStop));
                currentIterStop = connectionAssociatedWithCurrentIterStop.getDepStop();
            } else {
                System.err.println("Error in route reconstruction: No parent connection or transfer found for stop " + currentIterStop.getStopID() +
                        " which is not the departure stop.");
                return ResultantRouteCSA.notFound();
            }
        }

        Collections.reverse(segments);
        return ResultantRouteCSA.create(
                earliestArrival.get(csaQuery.getArrivalStop()),
                segments
        );
    }
}