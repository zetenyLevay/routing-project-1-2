package routing.routingEngineDijkstra.dijkstra.algorithm;

import routing.routingEngineDijkstra.dijkstra.model.input.*;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraFinalRoute;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraInputJourney;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraRouteStep;
import routing.routingEngineDijkstra.dijkstra.service.*;
import routing.routingEngineDijkstra.dijkstra.service.PathReconstructionService.SearchNode;

import java.time.LocalTime;
import java.util.*;

public class DijkstraRouter {
    private final Map<String, DijkstraStop> stops;
    private final Map<String, List<DijkstraConnection>> outgoingConnections;
    private final WalkingTransferService walkingService;
    private final PathReconstructionService reconstructionService;
    private final Map<String, DijkstraRouteInfo> routeInfo;
    private final int maxReasonableJourneyTime;

    public DijkstraRouter(Map<String, DijkstraStop> stops,
                          Map<String, List<DijkstraConnection>> outgoingConnections,
                          Map<String, DijkstraRouteInfo> routeInfo,
                          int maxWalkingDistanceMeters) {
        this.stops = stops;
        this.outgoingConnections = new HashMap<>(outgoingConnections);
        this.walkingService = new WalkingTransferService(new HaversineDistanceCalculator(), maxWalkingDistanceMeters);
        this.reconstructionService = new PathReconstructionService();
        this.routeInfo = routeInfo;
        this.maxReasonableJourneyTime = 4 * 3600; // 4 hours max

        // Pre-compute walking connections once during initialization
        precomputeWalkingConnections();
    }

    private void precomputeWalkingConnections() {
        for (DijkstraStop from : stops.values()) {
            List<DijkstraConnection> connections = this.outgoingConnections.computeIfAbsent(from.id, k -> new ArrayList<>());

            for (DijkstraStop to : stops.values()) {
                if (!from.equals(to) && walkingService.canWalkBetween(from, to)) {
                    // Create template walking connection (departure time will be set during search)
                    int walkTime = walkingService.calculateWalkTime(from, to);
                    DijkstraConnection walkConn = new DijkstraConnection(
                            from, to, 0, walkTime, null, "WALK", "Walk to " + to.name
                    );
                    connections.add(walkConn);
                }
            }
        }
    }

    public Journey findShortestJourney(String fromStopId, String toStopId, LocalTime departureTime) {
        return findShortestJourney(fromStopId, toStopId, departureTime.toSecondOfDay());
    }

    public Journey findShortestJourney(String fromStopId, String toStopId, int departureTimeSec) {
        DijkstraStop start = stops.get(fromStopId);
        DijkstraStop end = stops.get(toStopId);

        if (start == null || end == null) {
            return null;
        }

        DijkstraSearchManager searchManager = new DijkstraSearchManager();
        searchManager.initialize(start, departureTimeSec);

        while (!searchManager.isEmpty()) {
            SearchNode current = searchManager.getNextNode();
            if (current == null) break;

            // Early termination for unreasonably long journeys
            if (current.time > departureTimeSec + maxReasonableJourneyTime) {
                break;
            }

            if (current.stop.equals(end)) {
                return reconstructionService.reconstructJourney(current, departureTimeSec);
            }

            processConnections(current, searchManager);
        }
        return null;
    }

    private void processConnections(SearchNode current, DijkstraSearchManager searchManager) {
        List<DijkstraConnection> connections = outgoingConnections.getOrDefault(current.stop.id, Collections.emptyList());

        for (DijkstraConnection conn : connections) {
            DijkstraConnection actualConnection;

            if ("WALK".equals(conn.routeId)) {
                // For walking connections, adjust departure time to current time
                actualConnection = new DijkstraConnection(
                        conn.from, conn.to, current.time, current.time + conn.getDuration(),
                        conn.tripId, conn.routeId, conn.headSign
                );
            } else {
                // For transit connections, only use if departure time is valid
                if (conn.departureTime < current.time) continue;
                actualConnection = conn;
            }

            SearchNode newNode = new SearchNode(actualConnection.to, actualConnection.arrivalTime, current, actualConnection);
            searchManager.tryAddNode(newNode);
        }
    }

    public static class Journey {
        public final List<JourneyLeg> legs;
        public final int departureTime;
        public final int arrivalTime;

        public Journey(List<JourneyLeg> legs, int departureTime, int arrivalTime) {
            this.legs = Collections.unmodifiableList(legs);
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        public int getTotalTravelTime() {
            return arrivalTime - departureTime;
        }
    }

    public static class JourneyLeg {
        public final DijkstraStop from;
        public final DijkstraStop to;
        public final int departureTime;
        public final int arrivalTime;
        public final String routeId;
        public final String tripId;
        public final String headSign;
        public final boolean isWalking;

        public JourneyLeg(DijkstraStop from, DijkstraStop to, int departureTime, int arrivalTime,
                          String routeId, String tripId, String headSign, boolean isWalking) {
            this.from = from;
            this.to = to;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.routeId = routeId;
            this.tripId = tripId;
            this.headSign = headSign;
            this.isWalking = isWalking;
        }

        public int getDuration() {
            return arrivalTime - departureTime;
        }
    }

    public DijkstraFinalRoute findRoute(DijkstraInputJourney inputJourney) {
        DijkstraStop fromStop = walkingService.findClosestStop(inputJourney.getStart(), stops.values());
        DijkstraStop toStop = walkingService.findClosestStop(inputJourney.getEnd(), stops.values());

        int departureSec = inputJourney.getStartTime().toSecondOfDay();
        Journey journey = findShortestJourney(fromStop.id, toStop.id, departureSec);

        if (journey == null) return null;

        List<DijkstraRouteStep> steps = new ArrayList<>();
        int adjustedStartTime = departureSec;

        // Add initial walking step if needed
        if (!isSameCoordinate(inputJourney.getStart(), fromStop)) {
            int distance = walkingService.getDistance(inputJourney.getStart(), fromStop);
            double duration = distance / 1.389;
            steps.add(new DijkstraRouteStep("WALK", inputJourney.getStart(), toCoord(fromStop), duration));
            adjustedStartTime += duration;
        }

        // Add journey legs
        for (JourneyLeg leg : journey.legs) {
            String mode = leg.isWalking ? "WALK" : leg.routeId;
            if (leg.isWalking) {
                steps.add(new DijkstraRouteStep(mode, toCoord(leg.from), toCoord(leg.to), leg.getDuration()));
            } else {
                DijkstraRouteInfo info = routeInfo.get(leg.routeId);
                steps.add(new DijkstraRouteStep(mode, toCoord(leg.from), toCoord(leg.to),
                        leg.getDuration(), leg.to.name, info));
            }
        }

        // Add final walking step if needed
        DijkstraCoordinates endCoord = inputJourney.getEnd();
        DijkstraCoordinates toStopCoord = toCoord(toStop);

        if (!isSameCoordinate(endCoord, toStopCoord)) {
            int walkDist = walkingService.getDistance(toStop, endCoord);
            double walkTime = walkDist / 1.389;
            steps.add(new DijkstraRouteStep("WALK", toStopCoord, endCoord, walkTime));
        }

        double totalTime = steps.stream().mapToDouble(DijkstraRouteStep::getTime).sum();
        double totalDistance = steps.stream()
                .mapToDouble(step -> walkingService.getDistance(step.getStartCoord(), step.getEndCoord()))
                .sum();

        return new DijkstraFinalRoute(steps, totalDistance, totalTime);
    }

    private boolean isSameCoordinate(DijkstraCoordinates a, DijkstraStop b) {
        return a.getLatitude() == b.lat && a.getLongitude() == b.lon;
    }

    private DijkstraCoordinates toCoord(DijkstraStop stop) {
        return new DijkstraCoordinates(stop.lat, stop.lon);
    }

    private boolean isSameCoordinate(DijkstraCoordinates a, DijkstraCoordinates b) {
        return a.getLatitude() == b.getLatitude() && a.getLongitude() == b.getLongitude();
    }
}