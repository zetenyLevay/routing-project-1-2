package heatmap;

import routing.api.RoutingStrategy;
import routing.routingEngineModels.Stop.Stop;

public class Heatmap {
    private final HeatmapGenerator generator;

    public Heatmap(RoutingStrategy routingStrategy) {
        this.generator = new HeatmapGenerator(routingStrategy);
    }

    public HeatmapData createHeatmap(Stop originStop) {
        return generator.generateHeatmap(originStop);
    }

    public HeatmapData createHeatmap(String originStopId) {
        return generator.generateHeatmap(originStopId);
    }
}