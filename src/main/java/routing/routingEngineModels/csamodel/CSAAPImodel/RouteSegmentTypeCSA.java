package routing.routingEngineModels.csamodel.CSAAPImodel;

public enum RouteSegmentTypeCSA {
    TRANSIT(0,"TRANSFER WITHIN SAME STATION"),
    TRANSFER(1,"TRANSFER TO DIFFERENT STATION");

    private final int code;
    private final String name;

    RouteSegmentTypeCSA(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RouteSegmentTypeCSA getRouteSegmentTypeCSA(int code) {
        for (RouteSegmentTypeCSA routeSegmentTypeCSA : RouteSegmentTypeCSA.values()) {
            if (routeSegmentTypeCSA.code == code) {
                return routeSegmentTypeCSA;
            }
        }
        throw new IllegalArgumentException("Illegal code: " + code);
    }
}
