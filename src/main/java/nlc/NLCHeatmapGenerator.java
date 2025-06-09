package nlc;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.dijkstra.service.HaversineDistanceCalculator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NLCHeatmapGenerator {
    private static final int NEIGHBOR_RADIUS_METERS = 400;
    private final HaversineDistanceCalculator distanceCalculator = new HaversineDistanceCalculator();

    public NLCHeatmapData generate(AdiStop closedStop) {
        Map<String, AdiStop> stopMap = StopsCache.getAllStops();
        Collection<AdiStop> allStops = stopMap.values();
        Map<AdiStop, Integer> connectionLoss = new ConcurrentHashMap<>();

        // Precompute: For each stop, who are its neighbors?
        Map<AdiStop, Set<AdiStop>> neighborsMap = new ConcurrentHashMap<>();
        for (AdiStop s : allStops) {
            Set<AdiStop> neighbors = new HashSet<>();
            for (AdiStop other : allStops) {
                if (!s.equals(other) && calculateDistance(s, other) <= NEIGHBOR_RADIUS_METERS) {
                    neighbors.add(other);
                }
            }
            neighborsMap.put(s, neighbors);
        }

        for (AdiStop s : allStops) {
            if (s.equals(closedStop)) {
                connectionLoss.put(s, 0);
                continue;
            }

            Set<AdiStop> sNeighbors = neighborsMap.getOrDefault(s, Collections.emptySet());
            int lostLinks = 0;
            for (AdiStop n : sNeighbors) {
                if (n.equals(closedStop)) {
                    lostLinks++;
                } else {
                    // Check if closedStop was a bridge between s and n (both connected to closedStop, but not directly to each other)
                    boolean nIsNowUnreachable =
                            calculateDistance(s, n) > NEIGHBOR_RADIUS_METERS &&
                                    neighborsMap.getOrDefault(n, Collections.emptySet()).contains(closedStop);
                    if (nIsNowUnreachable) lostLinks++;
                }
            }

            connectionLoss.put(s, lostLinks);
        }

        return new NLCHeatmapData(closedStop, connectionLoss);
    }

    private int calculateDistance(AdiStop stop1, AdiStop stop2) {
        return distanceCalculator.calculateDistanceMeters(
                stop1.getCoordinates().getLatitude(),
                stop1.getCoordinates().getLongitude(),
                stop2.getCoordinates().getLatitude(),
                stop2.getCoordinates().getLongitude()
        );
    }
}
