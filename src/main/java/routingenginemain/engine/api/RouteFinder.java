package routingenginemain.engine.api;

import routingenginemain.model.MainAPImodel.ResultantRoute;

public interface RouteFinder {
    void acceptRequest();
    ResultantRoute findRoute();
    void displayResultantRoute();

}
