package heatmap;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineModels.FinalRoute;

/**
 * Generates heatmap data by calculating travel times from an origin stop to all other stops using a router.
 */
public class HeatmapGenerator {
    private final Router router;

    /**
     * Constructs a HeatmapGenerator with the specified router and initializes the stops cache.
     *
     * @param router the router used to compute travel times
     */
    public HeatmapGenerator(Router router) {
        this.router = router;
        StopsCache.init();
    }

    /**
     * Generates heatmap data from a specified origin stop.
     *
     * @param originStop the starting stop for the heatmap
     * @return a HeatmapData object containing travel times and color mappings
     */
    public HeatmapData generate(AdiStop originStop) {
        Map<AdiStop, Double> travelTimes = new ConcurrentHashMap<>();

        StopsCache.getAllStops().values().parallelStream()
                .filter(stop -> !stop.equals(originStop))
                .forEach(targetStop -> {
                    FinalRoute route = router.findRoute(
                            originStop.getCoordinates(),
                            targetStop.getCoordinates(),
                            LocalTime.of(8, 0)
                    );
                    if (route != null) {
                        travelTimes.put(targetStop, (double) route.getTotalTime());
                    }
                });

        return new HeatmapData(originStop, travelTimes);
    }

    /**
     * Generates heatmap data from a specified origin stop ID.
     *
     * @param originStopId the ID of the starting stop
     * @return a HeatmapData object containing travel times and color mappings
     * @throws IllegalArgumentException if the stop ID is not found
     */
    public HeatmapData generate(String originStopId) {
        AdiStop originStop = StopsCache.getStop(originStopId);
        if (originStop == null) {
            throw new IllegalArgumentException("Stop not found: " + originStopId);
        }
        return generate(originStop);
    }
}