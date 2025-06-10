package routing.routingEngineDijkstra.api;

import java.time.LocalTime;

import routing.api.RoutingStrategy;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter;
import routing.routingEngineDijkstra.dijkstra.converters.DijkstraModelConverter;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraFinalRoute;
import routing.routingEngineDijkstra.dijkstra.model.output.DijkstraInputJourney;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;

//TODO: FIX WALKING LOGIC

/**
 * Implements the RoutingStrategy interface using Dijkstra's algorithm to plan routes between stops or coordinates.
 */
public class DijkstraRoutePlanner implements RoutingStrategy {
    private final DijkstraRouter router;

    /**
     * Constructs a DijkstraRoutePlanner with a specified DijkstraRouter.
     *
     * @param router the DijkstraRouter used for route calculations
     */
    public DijkstraRoutePlanner(DijkstraRouter router) {
        this.router = router;
    }

    /**
     * Finds a route based on the provided InputJourney.
     *
     * @param inputJourney the journey details including start, end, and start time
     * @return a FinalRoute object representing the calculated route, or null if no route is found
     */
    @Override
    public FinalRoute findRoute(InputJourney inputJourney) {
        DijkstraInputJourney dijkstraInput = DijkstraModelConverter.toDijkstraInputJourney(inputJourney);

        DijkstraFinalRoute dijkstraResult = router.findRoute(dijkstraInput);

        if (dijkstraResult == null) {
            return null;
        }
        return DijkstraModelConverter.toFinalRoute(dijkstraResult, inputJourney.getStartTime());
    }

    /**
     * Finds a route between two stops starting at a specified time.
     *
     * @param from      the starting stop
     * @param to        the destination stop
     * @param startTime the start time of the journey
     * @return a FinalRoute object representing the calculated route, or null if no route is found
     */
    @Override
    public FinalRoute findRoute(AdiStop from, AdiStop to, LocalTime startTime) {
        InputJourney inputJourney = new InputJourney(
                new Coordinates(from.getLatitude(), from.getLongitude()),
                new Coordinates(to.getLatitude(), to.getLongitude()),
                startTime
        );
        return findRoute(inputJourney);
    }

    /**
     * Finds a route between two coordinates starting at a specified time.
     *
     * @param from      the starting coordinates
     * @param to        the destination coordinates
     * @param startTime the start time of the journey
     * @return a FinalRoute object representing the calculated route, or null if no route is found
     */
    public FinalRoute findRoute(Coordinates from, Coordinates to, LocalTime startTime) {
        InputJourney inputJourney = new InputJourney(from, to, startTime);
        return findRoute(inputJourney);
    }
}