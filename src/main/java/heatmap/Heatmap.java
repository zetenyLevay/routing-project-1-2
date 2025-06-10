package heatmap;

import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * Facade class for generating heatmap data based on travel times from a specified origin stop.
 */
public class Heatmap {
    private final HeatmapGenerator generator;

    /**
     * Constructs a Heatmap with the specified router for route calculations.
     *
     * @param router the router used to compute travel times
     */
    public Heatmap(Router router) {
        this.generator = new HeatmapGenerator(router);
    }

    /**
     * Creates heatmap data from a specified origin stop.
     *
     * @param originStop the starting stop for the heatmap
     * @return a HeatmapData object containing travel times and color mappings
     */
    public HeatmapData createFromStop(AdiStop originStop) {
        return generator.generate(originStop);
    }

    /**
     * Creates heatmap data from a specified origin stop ID.
     *
     * @param originStopId the ID of the starting stop
     * @return a HeatmapData object containing travel times and color mappings
     * @throws IllegalArgumentException if the stop ID is not found
     */
    public HeatmapData createFromStopId(String originStopId) {
        return generator.generate(originStopId);
    }
}