package routing.csa.engine.api;

import routing.routingEngineModels.csamodel.MainAPImodel.ResultantRoute;

public interface RouteFinder {
    void acceptRequest();
    ResultantRoute findRoute();
    void displayResultantRoute();

}
