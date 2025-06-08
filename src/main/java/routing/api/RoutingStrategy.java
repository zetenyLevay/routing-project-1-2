package routing.api;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;
import routing.routingEngineModels.Stop.Stop;
import java.time.LocalTime;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;


public interface RoutingStrategy {
    FinalRoute findRoute(InputJourney inputJourney);
    FinalRoute findRoute(AdiStop from, AdiStop to, LocalTime startTime);
}