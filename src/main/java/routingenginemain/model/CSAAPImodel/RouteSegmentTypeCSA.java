package routingenginemain.model.CSAAPImodel;

public enum RouteSegmentTypeCSA {
    TRANSIT(0,"Transit"),
    TRANSFER(1,"Transfer");

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
