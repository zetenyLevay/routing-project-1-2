package heatmap;

import heatmap.HeatmapData;
import heatmap.TravelTimeHeatmapAPI;
import routing.api.Router;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;

import java.sql.SQLException;
import java.util.Map;

public class HeatmapExample {
    public static void main(String[] args) throws SQLException {
        DijkstraRouter dijkstraRouter = GTFSDatabaseParser.createRouterFromGTFS(500);
        Router router = new Router(new DijkstraRoutePlanner(dijkstraRouter));

        TravelTimeHeatmapAPI heatmapAPI = new TravelTimeHeatmapAPI(router);
        HeatmapData heatmap = heatmapAPI.generateHeatmap("002133");
        Map<String, Double> allTimes = heatmapAPI.getAllTravelTimes(heatmap);
        allTimes.forEach((stopId, time) ->
                System.out.println(stopId + ": " + time + " minutes"));
    }
}