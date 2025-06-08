package routing.tests;

import routing.api.Router;
import routing.routingEngineDijkstra.api.DijkstraRoutePlanner;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.parsers.GTFSDatabaseParser;
import routing.routingEngineDijkstra.formatter.RouteFormatter;
import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;

import java.sql.SQLException;
import java.util.Scanner;

public class DetailedTest {
    public static void main(String[] args) {
        try {
            // Read JSON input from stdin (all in one line)
            Scanner scanner = new Scanner(System.in);
            String jsonInput = scanner.nextLine().trim();

            // Parse input
            InputJourney journey = RouteFormatter.parseInput(jsonInput);

            // Initialize router
            DijkstraRouter dijkstraRouter = GTFSDatabaseParser.createRouterFromGTFS(500);
            Router router = new Router(new DijkstraRoutePlanner(dijkstraRouter));

            // Find route
            FinalRoute route = router.findRoute(journey);

            // Format and print result
            System.out.println(RouteFormatter.formatResult(route, journey.getStartTime()));

        } catch (IllegalArgumentException e) {
            System.out.println("{\"error\":\"Invalid input format\"}");
        } catch (SQLException e) {
            System.out.println("{\"error\":\"Failed to load GTFS data\"}");
        } catch (Exception e) {
            System.out.println("{\"error\":\"Routing failed\"}");
        }
    }
}