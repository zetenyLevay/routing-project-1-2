package com.group.routingenginemain.model.route;

import com.group.routingenginemain.model.Agency;

public class Route {
    private final String routeID;
    private final Agency agency;
    private final RouteInfo routeInfo;

    public Route(String routeID, Agency agency, RouteInfo routeInfo) {
        this.routeID = routeID;
        this.agency = agency;
        this.routeInfo = routeInfo;
    }

    public String getRouteID() {
        return routeID;
    }

    public Agency getAgency() {
        return agency;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }
}
