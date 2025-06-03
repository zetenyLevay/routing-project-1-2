package routing.routingEngineDijkstra.newDijkstra.tests;

import routing.routingEngineDijkstra.newDijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.newDijkstra.model.input.Coordinates;
import routing.routingEngineDijkstra.newDijkstra.model.output.FinalRoute;
import routing.routingEngineDijkstra.newDijkstra.model.output.InputJourney;
import routing.routingEngineDijkstra.newDijkstra.parsers.GTFSDatabaseParser;

import java.sql.SQLException;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        try {
            DijkstraRouter router = GTFSDatabaseParser.createRouterFromGTFS(1000);
            DijkstraRoutePlanner planner = new DijkstraRoutePlanner(router);

            Coordinates startCoord = new Coordinates(47.513569, 18.970303

                    );
            Coordinates endCoord = new Coordinates(47.468691, 19.175723);
            LocalTime departureTime = LocalTime.of(6, 7);

            InputJourney inputJourney = new InputJourney(startCoord, endCoord, departureTime);
            FinalRoute route = planner.computeRoute(inputJourney);

            FinalRoutePrinter.print(route);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
