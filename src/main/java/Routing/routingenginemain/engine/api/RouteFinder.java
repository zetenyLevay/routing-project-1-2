package Routing.routingenginemain.engine.api;

import Routing.routingenginemain.model.MainAPImodel.ResultantRoute;

public interface RouteFinder {
    void acceptRequest();
    ResultantRoute findRoute();
    void displayResultantRoute();

}
