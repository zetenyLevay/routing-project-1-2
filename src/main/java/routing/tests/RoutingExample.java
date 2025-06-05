package routing.tests;

import routing.api.Router;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Random;

public class RoutingExample {
    public static void main(String[] args) {
        try {
            DijkstraRouter dijkstraRouter = GTFSDatabaseParser.createRouterFromGTFS(300);
            DijkstraRoutePlanner strategy = new DijkstraRoutePlanner(dijkstraRouter);
            Router router = new Router(strategy);

            int testCount = 50;
            Random random = new Random(42);

            double minLat = 47.3, maxLat = 47.6;
            double minLon = 18.9, maxLon = 19.3;

            long startTime = System.nanoTime();

            for (int i = 0; i < testCount; i++) {
                Coordinates from = new Coordinates(
                        minLat + (maxLat - minLat) * random.nextDouble(),
                        minLon + (maxLon - minLon) * random.nextDouble()
                );
                Coordinates to = new Coordinates(
                        minLat + (maxLat - minLat) * random.nextDouble(),
                        minLon + (maxLon - minLon) * random.nextDouble()
                );
                InputJourney journey = new InputJourney(from, to, LocalTime.of(6, 0));
                FinalRoute route = router.findRoute(journey);
            }

            long endTime = System.nanoTime();
            double totalMillis = (endTime - startTime) / 1_000_000.0;
            double averageMillis = totalMillis / testCount;

            System.out.printf("Ran %d tests%n", testCount);
            System.out.printf("Total time: %.2f ms%n", totalMillis);
            System.out.printf("Average time per test: %.2f ms%n", averageMillis);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load GTFS data or find route.");
        }
    }
}
