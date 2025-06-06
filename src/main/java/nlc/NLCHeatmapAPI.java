package nlc;

import heatmap.StopsCache;
import routing.routingEngineModels.Stop.Stop;

import java.awt.Color;
import java.util.Map;
import java.util.stream.Collectors;

public class NLCHeatmapAPI {
    private final NLCHeatmap heatmap;

    public NLCHeatmapAPI() {
        StopsCache.init();
        this.heatmap = new NLCHeatmap();
    }

    public NLCHeatmapData generateHeatmap(String closedStopId) {
        return heatmap.createFromStopId(closedStopId);
    }

    public int getNLCValue(NLCHeatmapData heatmapData, String stopId) {
        Stop stop = StopsCache.getStop(stopId);
        if (stop == null) return -1;
        return heatmapData.getNlcValues().getOrDefault(stop, 0);
    }

    public Color getStopColor(NLCHeatmapData heatmapData, String stopId) {
        Stop stop = StopsCache.getStop(stopId);
        if (stop == null) return Color.GRAY;
        return heatmapData.getStopColors().getOrDefault(stop, Color.GRAY);
    }

    public Map<String, Integer> getAllNLCValues(NLCHeatmapData heatmapData) {
        return heatmapData.getNlcValues().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }

    public Map<String, Color> getAllStopColors(NLCHeatmapData heatmapData) {
        return heatmapData.getStopColors().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getStopID(),
                        Map.Entry::getValue
                ));
    }
}
