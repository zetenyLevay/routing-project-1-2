package nlc;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

import heatmap.StopsCache;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * API for generating and querying Network Loss of Connectivity (NLC) heatmaps.
 */
public class NLCHeatmapAPI {
    private final NLCHeatmap heatmap;

    /**
     * Constructs an NLCHeatmapAPI and initializes the stops cache and heatmap generator.
     */
    public NLCHeatmapAPI() {
        StopsCache.init();
        this.heatmap = new NLCHeatmap();
    }

    /**
     * Generates an NLC heatmap for a specified closed stop ID.
     *
     * @param closedStopId the ID of the closed stop
     * @return an NLCHeatmapData object containing connectivity loss values and color mappings
     * @throws IllegalArgumentException if the stop ID is not found
     */
    public NLCHeatmapData generateHeatmap(String closedStopId) {
        return heatmap.createFromStopId(closedStopId);
    }

    /**
     * Retrieves the NLC value for a specific stop from an NLC heatmap.
     *
     * @param heatmapData the NLC heatmap data
     * @param stopId      the ID of the target stop
     * @return the NLC value for the stop, or -1 if the stop is not found
     */
    public int getNLCValue(NLCHeatmapData heatmapData, String stopId) {
        AdiStop stop = StopsCache.getStop(stopId);
        if (stop == null) return -1;
        return heatmapData.getNlcValues().getOrDefault(stop, 0);
    }

    /**
     * Retrieves the color assigned to a specific stop in an NLC heatmap.
     *
     * @param heatmapData the NLC heatmap data
     * @param stopId      the ID of the target stop
     * @return the Color for the stop, or Color.GRAY if the stop is not found
     */
    public Color getStopColor(NLCHeatmapData heatmapData, String stopId) {
        AdiStop stop = StopsCache.getStop(stopId);
        if (stop == null) return Color.GRAY;
        return heatmapData.getStopColors().getOrDefault(stop, Color.GRAY);
    }

    /**
     * Retrieves all NLC values from an NLC heatmap, mapped by stop IDs.
     *
     * @param heatmapData the NLC heatmap data
     * @return a map of stop IDs to NLC values
     */
    public Map<String, Integer> getAllNLCValues(NLCHeatmapData heatmapData) {
        return heatmapData.getNlcValues().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }

    /**
     * Retrieves all stop colors from an NLC heatmap, mapped by stop IDs.
     *
     * @param heatmapData the NLC heatmap data
     * @return a map of stop IDs to Color objects
     */
    public Map<String, Color> getAllStopColors(NLCHeatmapData heatmapData) {
        return heatmapData.getStopColors().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }
}