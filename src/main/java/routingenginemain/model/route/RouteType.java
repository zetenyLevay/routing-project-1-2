package routingenginemain.model.route;

public enum RouteType {
    TRAM(0, "Tram"),
    SUBWAY(1, "Subway"),
    RAIL(2, "Rail"),
    BUS(3, "Bus"),
    FERRY(4, "Ferry"),
    CABLE_TRAM(5, "Cable Tram"),
    AERIAL_LIFT(6, "Aerial Lift"),
    FUNICULAR(7, "Funicular"),
    TROLLEYBUS(11, "Trolleybus"),
    MONORAIL(12, "Monorail");

    private final int code;
    private final String name;

    RouteType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RouteType getNameFromCode(int code) {
        for (RouteType routeType : RouteType.values()) {
            if (routeType.code == code) {
                return routeType;
            }
        }
        throw new IllegalArgumentException("Invalid : " + code);
    }

    public String showDisplayName() {
        return name;
    }

}
