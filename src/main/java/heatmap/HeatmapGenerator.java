package heatmap;

import routing.api.Router;
import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HeatmapGenerator {
    private final Router router;

    public HeatmapGenerator(Router router) {
        this.router = router;
        StopsCache.init();
    }

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
                    if (route != null) {travelTimes.put(targetStop, (double) route.getTotalTime());}
                });




        return new HeatmapData(originStop, travelTimes);
    }


    public HeatmapData generate(String originStopId) {


            AdiStop originStop = StopsCache.getStop(originStopId);
        if (originStop == null) {
                throw new IllegalArgumentException("Stop not found: " +originStopId);
        }
        return generate(originStop);

    }
}