package nlc;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * Represents Network Loss of Connectivity (NLC) heatmap data, including connectivity loss values and color mappings for stops relative to a closed stop.
 */
public class NLCHeatmapData {
    private final AdiStop closedStop;
    private final Map<AdiStop, Integer> nlcValues;
    private final Map<AdiStop, Color> stopColors;
    private int maxValue;

    /**
     * Constructs an NLCHeatmapData object with the specified closed stop and connectivity loss values.
     *
     * @param closedStop the stop considered closed for the heatmap
     * @param nlcValues  a map of stops to their connectivity loss values
     */
    public NLCHeatmapData(AdiStop closedStop, Map<AdiStop, Integer> nlcValues) {
        this.closedStop = closedStop;
        this.nlcValues = new HashMap<>(nlcValues);
        this.stopColors = new HashMap<>();
        computeColorGradient();
    }

    /**
     * Computes color gradients for each stop based on NLC values, using a green-to-red gradient.
     */
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

    /**
     * Retrieves the closed stop of the NLC heatmap.
     *
     * @return the closed AdiStop
     */
    public AdiStop getClosedStop() {
        return closedStop;
    }

    /**
     * Retrieves the map of NLC values for each stop.
     *
     * @return a map of AdiStop to NLC values
     */
    public Map<AdiStop, Integer> getNlcValues() {
        return nlcValues;
    }

    /**
     * Retrieves the map of colors assigned to each stop.
     *
     * @return a map of AdiStop to Color objects
     */
    public Map<AdiStop, Color> getStopColors() {
        return stopColors;
    }
}