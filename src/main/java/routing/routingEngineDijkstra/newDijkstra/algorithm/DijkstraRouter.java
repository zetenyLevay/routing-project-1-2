package routing.routingEngineDijkstra.newDijkstra.algorithm;

import routing.routingEngineDijkstra.newDijkstra.model.input.*;
import routing.routingEngineDijkstra.newDijkstra.model.output.FinalRoute;
import routing.routingEngineDijkstra.newDijkstra.model.output.InputJourney;
import routing.routingEngineDijkstra.newDijkstra.model.output.RouteStep;
import routing.routingEngineDijkstra.newDijkstra.service.*;
import routing.routingEngineDijkstra.newDijkstra.service.PathReconstructionService.SearchNode;

import java.time.LocalTime;
import java.util.*;

public class DijkstraRouter {
    private final Map<String, DijkstraStop> stops;
    private final Map<String, List<DijkstraConnection>> outgoingConnections;
    private final WalkingTransferService walkingService;
    private final PathReconstructionService reconstructionService;
    private final Map<String, DijkstraRouteInfo> routeInfo;

    public DijkstraRouter(Map<String, DijkstraStop> stops, Map<String, List<DijkstraConnection>> outgoingConnections, Map<String, DijkstraRouteInfo> routeInfo, int maxWalkingDistanceMeters) {
        this.stops = stops;
        this.outgoingConnections = outgoingConnections;
        this.walkingService = new WalkingTransferService(new HaversineDistanceCalculator(), maxWalkingDistanceMeters);
        this.reconstructionService = new PathReconstructionService();
        this.routeInfo = routeInfo;
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

            if (current.stop.equals(end)) {return reconstructionService.reconstructJourney(current, departureTimeSec);
            }
            processTransitConnections(current, searchManager);
            processWalkingConnections(current, searchManager);
        }
        return null;
    }

    private void processTransitConnections(SearchNode current, DijkstraSearchManager searchManager) {
        List<DijkstraConnection> connections = outgoingConnections.getOrDefault(current.stop.id, Collections.emptyList());
        for (DijkstraConnection conn : connections) {
            if (conn.departureTime >= current.time) {
                SearchNode newNode = new SearchNode(conn.to, conn.arrivalTime, current, conn);
                searchManager.tryAddNode(newNode);
            }
        }
    }
    private void processWalkingConnections(SearchNode current, DijkstraSearchManager searchManager) {
        for (DijkstraStop otherStop : stops.values()) {
            if (!otherStop.equals(current.stop) &&
                    walkingService.canWalkBetween(current.stop, otherStop)) {

                DijkstraConnection walkConnection = walkingService.createWalkingConnection(
                        current.stop, otherStop, current.time);
                SearchNode newNode = new SearchNode(otherStop, walkConnection.arrivalTime, current, walkConnection);
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

        public JourneyLeg(DijkstraStop from, DijkstraStop to, int departureTime, int arrivalTime, String routeId, String tripId, String headSign, boolean isWalking) {
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

    public FinalRoute findRoute(InputJourney inputJourney) {
        DijkstraStop fromStop = walkingService.findClosestStop(inputJourney.getStart(), stops.values());
        DijkstraStop toStop = walkingService.findClosestStop(inputJourney.getEnd(), stops.values());

        int departureSec = inputJourney.getStartTime().toSecondOfDay();
        Journey journey = findShortestJourney(fromStop.id, toStop.id, departureSec);

        if (journey == null) return null;

        List<RouteStep> steps = new ArrayList<>();
        int adjustedStartTime = departureSec;

        if (!isSameCoordinate(inputJourney.getStart(), fromStop)) {
            int distance = walkingService.getDistance(inputJourney.getStart(), fromStop);
            double duration = distance / 1.389;
            steps.add(new RouteStep("WALK", inputJourney.getStart(), toCoord(fromStop), duration));
            adjustedStartTime += duration;
        }

        for (JourneyLeg leg : journey.legs) {
            String mode = leg.isWalking ? "WALK" : leg.routeId;
            if (leg.isWalking) {
                steps.add(new RouteStep(mode, toCoord(leg.from), toCoord(leg.to), leg.getDuration()));
            } else {
                DijkstraRouteInfo info = routeInfo.get(leg.routeId);
                steps.add(new RouteStep(mode, toCoord(leg.from), toCoord(leg.to), leg.getDuration(), leg.to.name, info));
            }
        }

        Coordinates endCoord = inputJourney.getEnd();
        Coordinates toStopCoord = toCoord(toStop);

        if (!isSameCoordinate(endCoord, toStopCoord)) {
            int walkDist = walkingService.getDistance(toStop, endCoord);
            double walkTime = walkDist / 1.389;
            steps.add(new RouteStep("WALK", toStopCoord, endCoord, walkTime));
        }

        double totalTime = steps.stream().mapToDouble(RouteStep::getTime).sum();
        double totalDistance = steps.stream().mapToDouble(step -> walkingService.getDistance(step.getStartCoord(), step.getEndCoord())).sum();
        return new FinalRoute(steps, totalDistance, totalTime);
    }

    private boolean isSameCoordinate(Coordinates a, DijkstraStop b) {
        return a.getLatitude() == b.lat && a.getLongitude() == b.lon;
    }
    private Coordinates toCoord(DijkstraStop stop) {
        return new Coordinates(stop.lat, stop.lon);
    }
    private boolean isSameCoordinate(Coordinates a, Coordinates b) {
        return a.getLatitude() == b.getLatitude() && a.getLongitude() == b.getLongitude();
    }

}