package routing.routingEngineDijkstra.api;

import heatmap.Router;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.converters.DijkstraModelConverter;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraFinalRoute;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraInputJourney;
import routing.routingEngineModels.*;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;

public class DijkstraRoutePlanner implements Router {
    private final DijkstraRouter router;

    public DijkstraRoutePlanner(DijkstraRouter router) {
        this.router = router;
    }

    @Override
    public FinalRoute findRoute(InputJourney inputJourney) {
        DijkstraInputJourney dijkstraInput = DijkstraModelConverter.toDijkstraInputJourney(inputJourney);

        DijkstraFinalRoute dijkstraResult = router.findRoute(dijkstraInput);

        if (dijkstraResult == null) {
            return null;
        }
        return DijkstraModelConverter.toFinalRoute(dijkstraResult, inputJourney.getStartTime());
    }

    public FinalRoute findRoute(Stop from, Stop to, LocalTime startTime) {
        InputJourney inputJourney = new InputJourney(
                new Coordinates(from.getLatitude(), from.getLongitude()),
                new Coordinates(to.getLatitude(), to.getLongitude()),
                startTime
        );
        return findRoute(inputJourney);
    }

    public FinalRoute findRoute(Coordinates from, Coordinates to, LocalTime startTime) {
        InputJourney inputJourney = new InputJourney(from, to, startTime);
        return findRoute(inputJourney);
    }
}