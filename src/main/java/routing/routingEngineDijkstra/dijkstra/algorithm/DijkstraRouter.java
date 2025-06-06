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
    private final HaversineDistanceCalculator distanceCalculator;

    public DijkstraRouter(Map<String, DijkstraStop> stops,
                          Map<String, List<DijkstraConnection>> outgoingConnections,
                          Map<String, DijkstraRouteInfo> routeInfo,
                          int maxWalkingDistanceMeters) {
        this.stops = stops;
        this.outgoingConnections = new HashMap<>(outgoingConnections);
        this.distanceCalculator = new HaversineDistanceCalculator();
        this.walkingService = new WalkingTransferService(
                distanceCalculator,
                maxWalkingDistanceMeters,
                stops.values()
        );
        this.reconstructionService = new PathReconstructionService();
        this.routeInfo = routeInfo;
        this.maxReasonableJourneyTime = 4 * 3600; //if you travel more than 4 hrs in a city youre mad

        precomputeWalkingConnections();
    }

    private void precomputeWalkingConnections() {
        stops.values().parallelStream().forEach(from -> {
            List<DijkstraConnection> connections = new ArrayList<>();

            for (DijkstraStop to : stops.values()) {
                if (!from.equals(to) &&
                        distanceCalculator.estimateQuickDistance(from.lat, from.lon, to.lat, to.lon)
                                <= walkingService.getMaxWalkingDistance()) {

                    if (walkingService.canWalkBetween(from, to)) {
                        int walkTime = walkingService.calculateWalkTime(from, to);
                        connections.add(new DijkstraConnection(
                                from, to, 0, walkTime, null, "WALK", "Walk to " + to.name
                        ));
                    }
                }
            }

            if (!connections.isEmpty()) {
                synchronized (outgoingConnections) {
                    outgoingConnections.merge(from.id, connections, (oldList, newList) -> {
                        oldList.addAll(newList);
                        return oldList;
                    });
                }
            }
        });
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
        List<DijkstraConnection> connections = outgoingConnections.get(current.stop.id);
        if (connections == null) return;

        for (int i = 0; i < connections.size(); i++) { // faster than iterator
            DijkstraConnection conn = connections.get(i);
            int arrivalTime;

            if ("WALK".equals(conn.routeId)) {
                arrivalTime = current.time + conn.getDuration();
            } else {
                if (conn.departureTime < current.time) continue;
                arrivalTime = conn.arrivalTime;
            }

            if (arrivalTime < searchManager.getEarliestArrival(conn.to)) {
                DijkstraConnection actualConnection;
                if ("WALK".equals(conn.routeId)) {
                    actualConnection = new DijkstraConnection(
                            conn.from, conn.to, current.time, arrivalTime,
                            conn.tripId, conn.routeId, conn.headSign
                    );
                } else {
                    actualConnection = conn;
                }

                SearchNode newNode = new SearchNode(actualConnection.to, arrivalTime, current, actualConnection);
                searchManager.tryAddNode(newNode);
            }
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

        if (!isSameCoordinate(inputJourney.getStart(), fromStop)) {
            int distance = walkingService.getDistance(inputJourney.getStart(), fromStop);
            double duration = distance / 1.389;
            steps.add(new DijkstraRouteStep("WALK", inputJourney.getStart(), toCoord(fromStop), duration));
            adjustedStartTime += duration;
        }
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