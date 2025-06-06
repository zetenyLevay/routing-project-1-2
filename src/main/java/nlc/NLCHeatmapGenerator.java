package nlc;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.dijkstra.service.HaversineDistanceCalculator;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NLCHeatmapGenerator {
    private static final int NEIGHBOR_RADIUS_METERS = 400;
    private final HaversineDistanceCalculator distanceCalculator = new HaversineDistanceCalculator();

    public NLCHeatmapData generate(Stop closedStop) {
        Map<String, Stop> stopMap = StopsCache.getAllStops();
        Collection<Stop> allStops = stopMap.values();
        Map<Stop, Integer> nlcValues = new ConcurrentHashMap<>();

        for (Stop s : allStops) {
            if (s.equals(closedStop)) continue;
            int count = 0;

            for (Stop neighbor : allStops) {
                if (neighbor.equals(s) || neighbor.equals(closedStop)) continue;

                int distToS = distanceCalculator.calculateDistanceMeters(
                        s.getCoordinates().getLatitude(),
                        s.getCoordinates().getLongitude(),
                        neighbor.getCoordinates().getLatitude(),
                        neighbor.getCoordinates().getLongitude()
                );

                int distToClosed = distanceCalculator.calculateDistanceMeters(
                        closedStop.getCoordinates().getLatitude(),
                        closedStop.getCoordinates().getLongitude(),
                        neighbor.getCoordinates().getLatitude(),
                        neighbor.getCoordinates().getLongitude()
                );

                if (distToS <= NEIGHBOR_RADIUS_METERS && distToClosed > NEIGHBOR_RADIUS_METERS) {
                    count++;
                }
            }

            nlcValues.put(s, count);
        }

        return new NLCHeatmapData(closedStop, nlcValues);
    }
}
