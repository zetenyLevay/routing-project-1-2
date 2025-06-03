package routing.routingEngineDijkstra.newDijkstra.tests;

import routing.routingEngineDijkstra.newDijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.newDijkstra.model.output.FinalRoute;
import routing.routingEngineDijkstra.newDijkstra.model.output.InputJourney;

public class DijkstraRoutePlanner {
    private final DijkstraRouter router;

    public DijkstraRoutePlanner(DijkstraRouter router) {
        this.router = router;
    }

    public FinalRoute computeRoute(InputJourney inputJourney) {
        return router.findRoute(inputJourney);
    }
}
