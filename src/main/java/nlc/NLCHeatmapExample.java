package nlc;

import routing.routingEngineModels.Stop.Stop;

import java.awt.Color;
import java.util.Map;

public class NLCHeatmapExample {
    public static void main(String[] args) {
        String closedStopId = "008620";

        NLCHeatmapAPI api = new NLCHeatmapAPI();
        NLCHeatmapData heatmapData = api.generateHeatmap(closedStopId);

        System.out.println("Closed Stop: " + heatmapData.getClosedStop().getStopID());
        System.out.println("\nNeighbor Loss Count per Stop:");

        for (Map.Entry<String, Integer> entry : api.getAllNLCValues(heatmapData).entrySet()) {
            String stopId = entry.getKey();
            int value = entry.getValue();
            Color color = api.getStopColor(heatmapData, stopId);
            System.out.printf("Stop %s -> NLC: %d | Color: rgb(%d, %d, %d)%n",
                    stopId, value, color.getRed(), color.getGreen(), color.getBlue());
        }
    }
}
