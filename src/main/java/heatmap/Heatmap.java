package heatmap;

import routing.routingEngineModels.Stop.Stop;

public class Heatmap {
    private final HeatmapGenerator generator;

    public Heatmap(Router router) {
        this.generator = new HeatmapGenerator(router);
    }

    public HeatmapData createHeatmap(Stop originStop) {
        return generator.generateHeatmap(originStop);
    }

    public HeatmapData createHeatmap(String originStopId) {
        return generator.generateHeatmap(originStopId);
    }
}