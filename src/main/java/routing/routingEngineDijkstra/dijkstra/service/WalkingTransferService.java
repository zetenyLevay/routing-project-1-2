package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages walking transfers between stops, including distance calculations and nearby stop lookups.
 */
public class WalkingTransferService {
    private static final double AVERAGE_WALKING_SPEED_MS = 1.389;
    private final DistanceCalculator distanceCalculator;
    private final int maxWalkingDistanceMeters;
    private final Map<DijkstraStop, List<DijkstraStop>> nearbyStopsCache = new HashMap<>();
    private final GridIndex gridIndex;
    private final Map<String, Integer> distanceCache = new HashMap<>();

    /**
     * Constructs a WalkingTransferService with the specified distance calculator and stops.
     *
     * @param distanceCalculator      the calculator for distance computations
     * @param maxWalkingDistanceMeters the maximum walking distance in meters
     * @param allStops                the collection of all stops
     */
    public WalkingTransferService(DistanceCalculator distanceCalculator,
                                  int maxWalkingDistanceMeters,
                                  Collection<DijkstraStop> allStops) {
        this.distanceCalculator = distanceCalculator;
        this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
        this.gridIndex = new GridIndex(allStops, maxWalkingDistanceMeters);
    }

    /**
     * Precomputes nearby stops for each stop based on walking distance.
     *
     * @param stops the collection of stops to process
     */
    public void precomputeNearbyStops(Collection<DijkstraStop> stops) {
        for (DijkstraStop stop : stops) {
            nearbyStopsCache.put(stop,
                    stops.stream()
                            .filter(s -> !s.equals(stop))
                            .filter(s -> canWalkBetween(stop, s))
                            .collect(Collectors.toList())
            );
        }
    }

    /**
     * Retrieves nearby stops within walking distance of the given stop.
     *
     * @param stop the reference stop
     * @return a list of nearby DijkstraStop objects
     */
    public List<DijkstraStop> getNearbyStops(DijkstraStop stop) {
        return gridIndex.getNearbyStops(stop).stream()
                .filter(s -> !s.equals(stop))
                .filter(s -> canWalkBetween(stop, s))
                .collect(Collectors.toList());
    }

    /**
     * Checks if walking between two stops is possible within the maximum walking distance.
     *
     * @param from the starting stop
     * @param to   the destination stop
     * @return true if walking is possible, false otherwise
     */
    public boolean canWalkBetween(DijkstraStop from, DijkstraStop to) {
        String cacheKey = from.id + "-" + to.id;
        Integer cachedDistance = distanceCache.get(cacheKey);

        if (cachedDistance == null) {
            cachedDistance = distanceCalculator.calculateDistanceMeters(from, to);
            distanceCache.put(cacheKey, cachedDistance);
        }

        return cachedDistance <= maxWalkingDistanceMeters;
    }

    /**
     * Calculates the walking time between two stops.
     *
     * @param from the starting stop
     * @param to   the destination stop
     * @return the walking time in seconds
     */
    public int calculateWalkTime(DijkstraStop from, DijkstraStop to) {
        String cacheKey = from.id + "-" + to.id;
        Integer cachedDistance = distanceCache.get(cacheKey);

        if (cachedDistance == null) {
            cachedDistance = distanceCalculator.calculateDistanceMeters(from, to);
            distanceCache.put(cacheKey, cachedDistance);
        }

        return (int) Math.round(cachedDistance / AVERAGE_WALKING_SPEED_MS);
    }

    /**
     * Creates a walking connection between two stops starting at a given time.
     *
     * @param from      the starting stop
     * @param to        the destination stop
     * @param startTime the start time in seconds since midnight
     * @return a DijkstraConnection representing the walking leg
     */
    public DijkstraConnection createWalkingConnection(DijkstraStop from, DijkstraStop to, int startTime) {
        int walkTime = calculateWalkTime(from, to);
        int arrivalTime = startTime + walkTime;
        return new DijkstraConnection(from, to, startTime, arrivalTime,
                null, "WALK", "Walk to " + to.name);
    }

    /**
     * Finds the closest stop to a given coordinate.
     *
     * @param coord      the reference coordinate
     * @param candidates the collection of candidate stops
     * @return the closest DijkstraStop
     * @throws NoSuchElementException if no candidates are provided
     */
    public DijkstraStop findClosestStop(DijkstraCoordinates coord, Collection<DijkstraStop> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(stop -> distanceCalculator.calculateDistanceMeters(coord, stop)))
                .orElseThrow();
    }

    /**
     * Calculates the distance between a stop and a coordinate.
     *
     * @param from the starting stop
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    public int getDistance(DijkstraStop from, DijkstraCoordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.lat, from.lon, to.getLatitude(), to.getLongitude());
    }

    /**
     * Calculates the distance between a coordinate and a stop.
     *
     * @param from the starting coordinate
     * @param to   the destination stop
     * @return the distance in meters
     */
    public int getDistance(DijkstraCoordinates from, DijkstraStop to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.lat, to.lon);
    }

    /**
     * Calculates the distance between two coordinates.
     *
     * @param from the starting coordinate
     * @param to   the destination coordinate
     * @return the distance in meters
     */
    public int getDistance(DijkstraCoordinates from, DijkstraCoordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

    /**
     * Retrieves the maximum walking distance.
     *
     * @return the maximum walking distance in meters
     */
    public int getMaxWalkingDistance() {
        return maxWalkingDistanceMeters;
    }
}