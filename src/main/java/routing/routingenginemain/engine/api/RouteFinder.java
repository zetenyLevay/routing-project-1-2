package routing.routingenginemain.engine.api;

import routing.routingenginemain.model.MainAPImodel.ResultantRoute;

public interface RouteFinder {
    void acceptRequest();
    ResultantRoute findRoute();
    void displayResultantRoute();

}
