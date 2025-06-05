package heatmap;

import routing.api.Router;
import routing.routingEngineModels.Stop.Stop;
import heatmap.StopsCache;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

public class TravelTimeHeatmapAPI {
    private final HeatmapGenerator heatmapGenerator;

    public TravelTimeHeatmapAPI(Router router) {
        // Initialize cache if not already loaded
        StopsCache.init();
        this.heatmapGenerator = new HeatmapGenerator(router);
    }

    public HeatmapData generateHeatmap(String originStopId) {
        Stop origin = StopsCache.getStop(originStopId);
        if (origin == null) {
            throw new IllegalArgumentException("Stop not found: " + originStopId);
        }
        return heatmapGenerator.generate(origin);
    }

    public double getTravelTime(HeatmapData heatmap, String targetStopId) {
        Stop target = StopsCache.getStop(targetStopId);
        if (target == null) return -1.0;
        return heatmap.getTravelTimes().getOrDefault(target, -1.0);
    }

    public Color getStopColor(HeatmapData heatmap, String targetStopId) {
        Stop target = StopsCache.getStop(targetStopId);
        if (target == null) return Color.GRAY;
        return heatmap.getStopColors().getOrDefault(target, Color.GRAY);
    }

    public Map<String, Double> getAllTravelTimes(HeatmapData heatmap) {
        return heatmap.getTravelTimes().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }

    public Map<String, Color> getAllStopColors(HeatmapData heatmap) {
        return heatmap.getStopColors().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }
}