package heatmap;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * Represents heatmap data, including travel times and color mappings for stops relative to an origin stop.
 */
public class HeatmapData {
    private final AdiStop originStop;
    private final Map<AdiStop, Double> travelTimes;
    private final Map<AdiStop, Color> stopColors;
    private double minTime;
    private double maxTime;

    /**
     * Constructs a HeatmapData object with the specified origin stop and travel times.
     *
     * @param originStop  the starting stop for the heatmap
     * @param travelTimes a map of stops to their travel times from the origin
     */
    public HeatmapData(AdiStop originStop, Map<AdiStop, Double> travelTimes) {
        this.originStop = originStop;
        this.travelTimes = new HashMap<>(travelTimes);
        this.stopColors = new HashMap<>();
        calculateColorGradient();
    }

    /**
     * Calculates color gradients for each stop based on travel times, using a green-to-red gradient.
     */
    private void calculateColorGradient() {
        this.minTime = travelTimes.values().stream()
                .filter(t -> t > 0)
                .min(Double::compare)
                .orElse(0.0);

        this.maxTime = travelTimes.values().stream()
                .max(Double::compare)
                .orElse(minTime + 1);

        travelTimes.forEach((stop, time) -> {
            if (stop.equals(originStop)) {
                stopColors.put(stop, Color.BLACK);
            } else {
                float normalized = (float) ((time - minTime) / (maxTime - minTime));
                normalized = Math.max(0, Math.min(1, normalized));
                double gamma = 0.8;
                double adjusted = Math.pow(normalized, gamma);
                stopColors.put(stop, ColorGradient.getGreenToRedGradient((float) adjusted));
            }
        });
    }

    /**
     * Retrieves the map of travel times to each stop.
     *
     * @return a map of AdiStop to travel times in seconds
     */
    public Map<AdiStop, Double> getTravelTimes() {
        return travelTimes;
    }

    /**
     * Retrieves the map of colors assigned to each stop.
     *
     * @return a map of AdiStop to Color objects
     */
    public Map<AdiStop, Color> getStopColors() {
        return stopColors;
    }

    /**
     * Retrieves the minimum travel time in the heatmap (excluding zero times).
     *
     * @return the minimum travel time in seconds
     */
    public double getMinTime() {
        return minTime;
    }

    /**
     * Retrieves the maximum travel time in the heatmap.
     *
     * @return the maximum travel time in seconds
     */
    public double getMaxTime() {
        return maxTime;
    }
}