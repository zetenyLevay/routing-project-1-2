package routing.csa.engine.api;

import routing.csa.model.MainAPImodel.ResultantRoute;

public interface RouteFinder {
    void acceptRequest();
    ResultantRoute findRoute();
    void displayResultantRoute();

}
