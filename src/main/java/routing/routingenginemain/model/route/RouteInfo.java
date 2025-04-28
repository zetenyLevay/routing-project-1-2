package routing.routingenginemain.model.route;

public class RouteInfo {
    private final String routeShortName;
    private final String routeDescription;
    private final RouteType routeType;

    public RouteInfo(String routeShortName, String routeDescription, int code) {
        this.routeShortName = routeShortName;
        this.routeDescription = routeDescription;
        this.routeType = RouteType.getNameFromCode(code);
    }


    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteDescription() {
        return routeDescription;
    }

    public String getRouteTypeName() {
        return routeType.showDisplayName();
    }
}
