package heatmap;

import routing.api.Router;
import routing.routingEngineDijkstra.adiModels.Stop.*;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API for generating and querying travel time heatmaps based on a router.
 */
public class TravelTimeHeatmapAPI {
    private final HeatmapGenerator heatmapGenerator;

    /**
     * Constructs a TravelTimeHeatmapAPI with the specified router and initializes the stops cache.
     *
     * @param router the router used to compute travel times
     */
    public TravelTimeHeatmapAPI(Router router) {
        StopsCache.init();
        this.heatmapGenerator = new HeatmapGenerator(router);
    }

    /**
     * Generates a travel time heatmap from a specified origin stop ID.
     *
     * @param originStopId the ID of the starting stop
     * @return a HeatmapData object containing travel times and color mappings
     * @throws IllegalArgumentException if the stop ID is not found
     */
    public HeatmapData generateHeatmap(String originStopId) {
        AdiStop origin = StopsCache.getStop(originStopId);
        if (origin == null) {
            throw new IllegalArgumentException("Stop not found: " + originStopId);
        }
        return heatmapGenerator.generate(origin);
    }

    /**
     * Retrieves the travel time to a target stop from a heatmap.
     *
     * @param heatmap      the heatmap data
     * @param targetStopId the ID of the target stop
     * @return the travel time in seconds, or -1.0 if the stop is not found or no route exists
     */
    public double getTravelTime(HeatmapData heatmap, String targetStopId) {
        AdiStop target = StopsCache.getStop(targetStopId);
        if (target == null) return -1.0;
        return heatmap.getTravelTimes().getOrDefault(target, -1.0);
    }

    /**
     * Retrieves the color assigned to a target stop in a heatmap.
     *
     * @param heatmap      the heatmap data
     * @param targetStopId the ID of the target stop
     * @return the Color for the stop, or Color.GRAY if the stop is not found or no route exists
     */
    public Color getStopColor(HeatmapData heatmap, String targetStopId) {
        AdiStop target = StopsCache.getStop(targetStopId);
        if (target == null) return Color.GRAY;
        return heatmap.getStopColors().getOrDefault(target, Color.GRAY);
    }

    /**
     * Retrieves all travel times from a heatmap, mapped by stop IDs.
     *
     * @param heatmap the heatmap data
     * @return a map of stop IDs to travel times in seconds
     */
    public Map<String, Double> getAllTravelTimes(HeatmapData heatmap) {
        return heatmap.getTravelTimes().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }

    /**
     * Retrieves all stop colors from a heatmap, mapped by stop IDs.
     *
     * @param heatmap the heatmap data
     * @return a map of stop IDs to Color objects
     */
    public Map<String, Color> getAllStopColors(HeatmapData heatmap) {
        return heatmap.getStopColors().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }
}