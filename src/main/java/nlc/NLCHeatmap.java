package nlc;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * Facade class for generating Network Loss of Connectivity (NLC) heatmap data based on a closed stop.
 */
public class NLCHeatmap {
    private final NLCHeatmapGenerator generator = new NLCHeatmapGenerator();

    /**
     * Creates NLC heatmap data from a specified closed stop.
     *
     * @param closedStop the stop considered closed for the heatmap
     * @return an NLCHeatmapData object containing connectivity loss values and color mappings
     */
    public NLCHeatmapData createFromStop(AdiStop closedStop) {
        return generator.generate(closedStop);
    }

    /**
     * Creates NLC heatmap data from a specified closed stop ID.
     *
     * @param stopId the ID of the closed stop
     * @return an NLCHeatmapData object containing connectivity loss values and color mappings
     * @throws IllegalArgumentException if the stop ID is not found
     */
    public NLCHeatmapData createFromStopId(String stopId) {
        AdiStop stop = StopsCache.getStop(stopId);
        if (stop == null) throw new IllegalArgumentException("Stop not found: " + stopId);
        return generator.generate(stop);
    }
}