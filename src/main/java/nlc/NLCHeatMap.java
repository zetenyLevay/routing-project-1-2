package nlc;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

public class NLCHeatMap {
    private final NLCHeatmapGenerator generator = new NLCHeatmapGenerator();

    /**
     * Creates NLC heatmap data from a specified closed stop.
     *
     * @param closedStop the stop considered closed for the heatmap
     * @return an NLCHeatmapData object containing connectivity loss values and color mappings
     * @throws IllegalArgumentException if closedStop is null
     */
    public NLCHeatmapData createFromStop(AdiStop closedStop) {
        if (closedStop == null) {
            throw new IllegalArgumentException("Closed stop cannot be null");
        }
        return generator.generate(closedStop);
    }

    /**
     * Creates NLC heatmap data from a specified closed stop ID.
     *
     * @param stopId the ID of the closed stop
     * @return an NLCHeatmapData object containing connectivity loss values and color mappings
     * @throws IllegalArgumentException if the stop ID is not found or is null/empty
     */
    public NLCHeatmapData createFromStopId(String stopId) {
        if (stopId == null || stopId.trim().isEmpty()) {
            throw new IllegalArgumentException("Stop ID cannot be null or empty");
        }

        AdiStop stop = StopsCache.getStop(stopId);
        if (stop == null) {
            throw new IllegalArgumentException("Stop not found: " + stopId +
                    ". Please check the stop ID and try again.");
        }
        return generator.generate(stop);
    }
}