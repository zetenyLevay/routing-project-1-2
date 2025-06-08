package nlc;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.adiModels.*;
import routing.routingEngineDijkstra.adiModels.Stop.*;

public class NLCHeatmap {
    private final NLCHeatmapGenerator generator = new NLCHeatmapGenerator();

    public NLCHeatmapData createFromStop(AdiStop closedStop) {
        return generator.generate(closedStop);
    }

    public NLCHeatmapData createFromStopId(String stopId) {
        AdiStop stop = StopsCache.getStop(stopId);
        if (stop == null) throw new IllegalArgumentException("Stop not found: " + stopId);
        return generator.generate(stop);
    }
}
