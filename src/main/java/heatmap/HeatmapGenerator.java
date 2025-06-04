package heatmap;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineCSA.engine.cache.classloader.StopsCache;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeatmapGenerator {
    private final Router router;

    public HeatmapGenerator(Router router) {
        this.router = router;
        StopsCache.init();
    }

    public HeatmapData generateHeatmap(Stop originStop) {
        Map<Stop, Double> travelTimes = new ConcurrentHashMap<>();

        StopsCache.getAllStops().parallelStream()
                .filter(stop -> !stop.equals(originStop))
                .forEach(targetStop -> {
                    FinalRoute route = router.findRoute(originStop, targetStop, LocalTime.of(8,0));
                    if (route != null) {
                        travelTimes.put(targetStop, route.getTotalTime());
                    }
                });

        return new HeatmapData(originStop, travelTimes);
    }

    public HeatmapData generateHeatmap(String originStopId) {
        Stop originStop = StopsCache.getStop(originStopId);
        if (originStop == null) {
            throw new IllegalArgumentException("Stop not found: " + originStopId);
        }
        return generateHeatmap(originStop);
    }
}