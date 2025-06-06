package nlc;

import routing.routingEngineModels.Stop.Stop;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class NLCHeatmapData {
    private final Stop closedStop;
    private final Map<Stop, Integer> nlcValues;
    private final Map<Stop, Color> stopColors;
    private int maxValue;

    public NLCHeatmapData(Stop closedStop, Map<Stop, Integer> nlcValues) {
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

    public Stop getClosedStop() { return closedStop; }
    public Map<Stop, Integer> getNlcValues() { return nlcValues; }
    public Map<Stop, Color> getStopColors() { return stopColors; }
}
