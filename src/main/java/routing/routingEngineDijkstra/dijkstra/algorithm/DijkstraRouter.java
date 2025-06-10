package routing.routingEngineDijkstra.dijkstra.algorithm;

import routing.routingEngineDijkstra.dijkstra.model.input.*;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraFinalRoute;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraInputJourney;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraRouteStep;
import routing.routingEngineDijkstra.dijkstra.service.*;
import routing.routingEngineDijkstra.dijkstra.service.PathReconstructionService.SearchNode;

import java.time.LocalTime;
import java.util.*;

/**
 * Implements Dijkstra's algorithm for finding the shortest journey between stops, incorporating walking and transit connections.
 */
public class DijkstraRouter {
    private final Map<String, DijkstraStop> stops;
    private final Map<String, List<DijkstraConnection>> outgoingConnections;
    private final WalkingTransferService walkingService;
    private final PathReconstructionService reconstructionService;
    private final Map<String, DijkstraRouteInfo> routeInfo;
    private final int maxReasonableJourneyTime;
    private final HaversineDistanceCalculator distanceCalculator;
    private final Map<String, DijkstraConnection[]> connectionArrayCache = new HashMap<>();

    /**
     * Constructs a DijkstraRouter with the specified stops, connections, route information, and maximum walking distance.
     *
     * @param stops                   the map of stop IDs to DijkstraStop objects
     * @param outgoingConnections     the map of stop IDs to lists of outgoing connections
     * @param routeInfo               the map of route IDs to DijkstraRouteInfo objects
     * @param maxWalkingDistanceMeters the maximum walking distance in meters
     */
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
        this.maxReasonableJourneyTime = 4 * 3600;

        precomputeWalkingConnections();
        precomputeConnectionArrays();
    }

    /**
     * Precomputes connection arrays for each stop, sorting connections by departure time.
     */
    private void precomputeConnectionArrays() {
        outgoingConnections.forEach((stopId, connections) -> {
            connections.sort(Comparator.comparingInt(c ->
                    "WALK".equals(c.routeId) ? 0 : c.departureTime));
            connectionArrayCache.put(stopId, connections.toArray(new DijkstraConnection[0]));
        });
    }

    /**
     * Precomputes walking connections between stops within the maximum walking distance.
     */
    private void precomputeWalkingConnections() {
        stops.values().parallelStream()
                .filter(stop -> !outgoingConnections.containsKey(stop.id) ||
                        outgoingConnections.get(stop.id).stream().noneMatch(c -> "WALK".equals(c.routeId)))
                .forEach(from -> {
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

    /**
     * Finds the shortest journey between two stops starting at a specified time.
     *
     * @param fromStopId    the ID of the starting stop
     * @param toStopId      the ID of the destination stop
     * @param departureTime the departure time as a LocalTime
     * @return a Journey object representing the shortest journey, or null if no journey is found
     */
    public Journey findShortestJourney(String fromStopId, String toStopId, LocalTime departureTime) {
        return findShortestJourney(fromStopId, toStopId, departureTime.toSecondOfDay());
    }

    /**
     * Finds the shortest journey between two stops starting at a specified time in seconds.
     *
     * @param fromStopId       the ID of the starting stop
     * @param toStopId         the ID of the destination stop
     * @param departureTimeSec the departure time in seconds since midnight
     * @return a Journey object representing the shortest journey, or null if no journey is found
     */
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

    /**
     * Processes outgoing connections from the current node in the Dijkstra search.
     *
     * @param current       the current search node
     * @param searchManager the search manager handling node exploration
     */
    private void processConnections(SearchNode current, DijkstraSearchManager searchManager) {
        DijkstraConnection[] connections = connectionArrayCache.get(current.stop.id);
        if (connections == null) return;

        for (DijkstraConnection conn : connections) {
            int arrivalTime;

            if ("WALK".equals(conn.routeId)) {
                arrivalTime = current.time + conn.getDuration();
            } else {
                if (conn.departureTime < current.time) continue;

                if (conn.departureTime > current.time + 1800) {
                    continue;
                }

                arrivalTime = conn.arrivalTime;
            }

            if (arrivalTime >= searchManager.getEarliestArrival(conn.to)) {
                continue;
            }

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

    /**
     * Represents a journey composed of multiple legs, with departure and arrival times.
     */
    public static class Journey {
        public final List<JourneyLeg> legs;
        public final int departureTime;
        public final int arrivalTime;

        /**
         * Constructs a Journey with the specified legs and times.
         *
         * @param legs          the list of journey legs
         * @param departureTime the departure time in seconds since midnight
         * @param arrivalTime   the arrival time in seconds since midnight
         */
        public Journey(List<JourneyLeg> legs, int departureTime, int arrivalTime) {
            this.legs = Collections.unmodifiableList(legs);
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        /**
         * Calculates the total travel time of the journey.
         *
         * @return the total travel time in seconds
         */
        public int getTotalTravelTime() {
            return arrivalTime - departureTime;
        }
    }

    /**
     * Represents a single leg of a journey, including start and end stops, times, and route details.
     */
    public static class JourneyLeg {
        public final DijkstraStop from;
        public final DijkstraStop to;
        public final int departureTime;
        public final int arrivalTime;
        public final String routeId;
        public final String tripId;
        public final String headSign;
        public final boolean isWalking;

        /**
         * Constructs a JourneyLeg with the specified details.
         *
         * @param from          the starting stop
         * @param to            the destination stop
         * @param departureTime the departure time in seconds
         * @param arrivalTime   the arrival time in seconds
         * @param routeId       the ID of the route, or "WALK" for walking
         * @param tripId        the ID of the trip, or null for walking
         * @param headSign      the head sign of the route
         * @param isWalking     true if this leg is a walking leg
         */
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

        /**
         * Calculates the duration of this leg.
         *
         * @return the duration in seconds
         */
        public int getDuration() {
            return arrivalTime - departureTime;
        }
    }

    /**
     * Finds a route based on the provided DijkstraInputJourney.
     *
     * @param inputJourney the journey details including start, end, and start time
     * @return a DijkstraFinalRoute object representing the calculated route, or null if no route is found
     */
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

    /**
     * Checks if a coordinate matches a stop's coordinates.
     *
     * @param a the DijkstraCoordinates to check
     * @param b the DijkstraStop to compare against
     * @return true if the coordinates are the same, false otherwise
     */
    private boolean isSameCoordinate(DijkstraCoordinates a, DijkstraStop b) {
        return a.getLatitude() == b.lat && a.getLongitude() == b.lon;
    }

    /**
     * Converts a DijkstraStop to DijkstraCoordinates.
     *
     * @param stop the stop to convert
     * @return a DijkstraCoordinates object representing the stop's location
     */
    private DijkstraCoordinates toCoord(DijkstraStop stop) {
        return new DijkstraCoordinates(stop.lat, stop.lon);
    }

    /**
     * Checks if two coordinates are the same.
     *
     * @param a the first DijkstraCoordinates
     * @param b the second DijkstraCoordinates
     * @return true if the coordinates are identical, false otherwise
     */
    private boolean isSameCoordinate(DijkstraCoordinates a, DijkstraCoordinates b) {
        return a.getLatitude() == b.getLatitude() && a.getLongitude() == b.getLongitude();
    }
}