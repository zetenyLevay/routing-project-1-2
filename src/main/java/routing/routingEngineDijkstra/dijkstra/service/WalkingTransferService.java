package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraCoordinates;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.Collection;
import java.util.Comparator;

public class WalkingTransferService {
    private static final double AVERAGE_WALKING_SPEED_MS = 1.389;
    private final DistanceCalculator distanceCalculator;
    private final int maxWalkingDistanceMeters;

    public WalkingTransferService(DistanceCalculator distanceCalculator, int maxWalkingDistanceMeters) {
        this.distanceCalculator = distanceCalculator;
        this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
    }

    public boolean canWalkBetween(DijkstraStop from, DijkstraStop to) {
        return distanceCalculator.calculateDistanceMeters(from, to) <= maxWalkingDistanceMeters;
    }

    public int calculateWalkTime(DijkstraStop from, DijkstraStop to) {
        int distance = distanceCalculator.calculateDistanceMeters(from, to);
        return (int) Math.round(distance / AVERAGE_WALKING_SPEED_MS);
    }

    public DijkstraConnection createWalkingConnection(DijkstraStop from, DijkstraStop to, int startTime) {
        int walkTime = calculateWalkTime(from, to);
        int arrivalTime = startTime + walkTime;
        return new DijkstraConnection(from, to, startTime, arrivalTime,
                null, "WALK", "Walk to " + to.name);
    }

    public DijkstraStop findClosestStop(DijkstraCoordinates coord, Collection<DijkstraStop> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(stop -> distanceCalculator.calculateDistanceMeters(coord, stop)))
                .orElseThrow();
    }

    public int getDistance(DijkstraStop from, DijkstraCoordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.lat, from.lon, to.getLatitude(), to.getLongitude());
    }

    public int getDistance(DijkstraCoordinates from, DijkstraStop to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.lat, to.lon);
    }

    public int getDistance(DijkstraCoordinates from, DijkstraCoordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }
}