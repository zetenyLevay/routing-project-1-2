package routing.api;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;

public class Router {
    private RoutingStrategy strategy;

    public Router(RoutingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RoutingStrategy strategy) {
        this.strategy = strategy;
    }

    public FinalRoute findRoute(InputJourney inputJourney) {
        return strategy.findRoute(inputJourney);
    }

    public FinalRoute findRoute(Stop from, Stop to, LocalTime startTime) {
        return strategy.findRoute(from, to, startTime);
    }

    public FinalRoute findRoute(Coordinates from, Coordinates to, LocalTime startTime) {
        InputJourney inputJourney = new InputJourney(from, to, startTime);
        return strategy.findRoute(inputJourney);
    }
}
