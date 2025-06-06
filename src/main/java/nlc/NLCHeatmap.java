package nlc;

import heatmap.StopsCache;
import routing.routingEngineModels.Stop.Stop;

public class NLCHeatmap {
    private final NLCHeatmapGenerator generator = new NLCHeatmapGenerator();

    public NLCHeatmapData createFromStop(Stop closedStop) {
        return generator.generate(closedStop);
    }

    public NLCHeatmapData createFromStopId(String stopId) {
        Stop stop = StopsCache.getStop(stopId);
        if (stop == null) throw new IllegalArgumentException("Stop not found: " + stopId);
        return generator.generate(stop);
    }
}
