package nlc;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class NLCHeatmapData {
    private final AdiStop closedStop;
    private final Map<AdiStop, Integer> nlcValues;
    private final Map<AdiStop, Color> stopColors;
    private int maxValue;

    public NLCHeatmapData(AdiStop closedStop, Map<AdiStop, Integer> nlcValues) {
        this.closedStop = closedStop;
        this.nlcValues = new HashMap<>(nlcValues);
        this.stopColors = new HashMap<>();
        computeColorGradient();
    }

    private void computeColorGradient() {
        this.maxValue = nlcValues.values().stream().max(Integer::compare).orElse(1);

        nlcValues.forEach((stop, value) -> {
            if (stop.equals(closedStop)) {
                stopColors.put(stop, Color.BLACK);
            } else {
                float normalized = (float) value / maxValue;
                stopColors.put(stop, ColorGradient.getGreenToRedGradient(normalized));
            }
        });
    }

    public AdiStop getClosedStop() { return closedStop; }
    public Map<AdiStop, Integer> getNlcValues() { return nlcValues; }
    public Map<AdiStop, Color> getStopColors() { return stopColors; }
}
