package heatmap;

import routing.api.Router;
import routing.routingEngineModels.Stop.Stop;

public class Heatmap {
    private final HeatmapGenerator generator;

    public Heatmap(Router router) {
        this.generator = new HeatmapGenerator(router);
    }

    public HeatmapData createFromStop(Stop originStop) {
        return generator.generate(originStop);
    }

    public HeatmapData createFromStopId(String originStopId) {
        return generator.generate(originStopId);
    }
}
