package routing.routingEngineDijkstra.newDijkstra.service;

import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraStop;

import java.util.Collection;
import java.util.Comparator;

public class WalkingTransferService {


    private static final double AVERAGE_WALKING_SPEED_MS = 1.389; // 5 km/h
    private final DistanceCalculator distanceCalculator;
    private final int maxWalkingDistanceMeters;

    public WalkingTransferService(DistanceCalculator distanceCalculator, int maxWalkingDistanceMeters) {
        this.distanceCalculator = distanceCalculator;
        this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
    }

    public boolean canWalkBetween(DijkstraStop from, DijkstraStop to) {


        return distanceCalculator.calculateDistanceMeters(from, to) <= maxWalkingDistanceMeters;
    }

            public DijkstraConnection createWalkingConnection(DijkstraStop from, DijkstraStop to, int startTime) {
        int distance = distanceCalculator.calculateDistanceMeters(from, to);
        int walkTime = (int) Math.round(distance / AVERAGE_WALKING_SPEED_MS);
        int arrivalTime = startTime + walkTime;
        return new DijkstraConnection(from, to, startTime, arrivalTime,
                null, "WALK", "Walk to " + to.name);
    }

    public DijkstraStop findClosestStop(Coordinates coord, Collection<DijkstraStop> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(stop -> distanceCalculator.calculateDistanceMeters(coord, stop)))
                .orElseThrow();
    }

    public int getDistance(DijkstraStop from, Coordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.lat, from.lon, to.getLatitude(), to.getLongitude());
    }

    public int getDistance(Coordinates from, DijkstraStop to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.lat, to.lon);
    }

    public int getDistance(Coordinates from, Coordinates to) {
        return distanceCalculator.calculateDistanceMeters(from.getLatitude(), from.getLongitude(), to.getLatitude(), to.getLongitude());
    }

}