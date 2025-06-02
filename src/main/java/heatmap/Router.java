package heatmap;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.Stop.Stop;

public interface Router {
    FinalRoute findRoute(Stop fromStop, Stop toStop);
}