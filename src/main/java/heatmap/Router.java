package heatmap;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;

public interface Router {
    FinalRoute findRoute(InputJourney inputJourney);

    FinalRoute findRoute(Stop from, Stop to, LocalTime localTime);
}