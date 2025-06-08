package heatmap;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class HeatmapData {
    private final AdiStop originStop;
    private final Map<AdiStop, Double> travelTimes;
    private final Map<AdiStop, Color> stopColors;
    private double minTime;
    private double maxTime;

    public HeatmapData(AdiStop originStop, Map<AdiStop, Double> travelTimes) {
        this.originStop = originStop;
        this.travelTimes = new HashMap<>(travelTimes);
        this.stopColors = new HashMap<>();
        calculateColorGradient();
    }

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

    public AdiStop getOriginStop() { return originStop; }
    public Map<AdiStop, Double> getTravelTimes() { return travelTimes; }
    public Map<AdiStop, Color> getStopColors() { return stopColors; }
    public double getMinTime() { return minTime; }
    public double getMaxTime() { return maxTime; }
}