package heatmap;

import routing.routingEngineModels.Stop.Stop;

import java.awt.*;

public class HeatmapExample {
   public static void main(String[] args) {
       Router router = new MyRouterImplementation(); //myrouterimplementation to be replaced by whatever algo is made
       Heatmap heatmap = new Heatmap(router);

       HeatmapData data = heatmap.createHeatmap("stop_id_123");

       System.out.println("Heatmap from: " + data.getOriginStop().getStopName());
       System.out.println("Time range: " + data.getMinTime() + "s to " + data.getMaxTime() + "s");

       data.getStopColors().entrySet().stream()
               .limit(5)
               .forEach(entry -> {
                   Stop stop = entry.getKey();
                   Color color = entry.getValue();
                   System.out.printf("Stop: %-20s | Time: %-6.1fs | Color: RGB(%d,%d,%d)%n",
                           stop.getStopName(),
                           data.getTravelTimes().get(stop),
                           color.getRed(),
                           color.getGreen(),
                           color.getBlue());
               });
   }
}