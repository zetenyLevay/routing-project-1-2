package routing.routingEngineModels;

/**
 * A simple container for the four required fields of a “routeInfo” JSON object:
 * { "operator": agency_name (from agency.txt), "shortName": route_short_name
 * (from routes.txt), "longName": route_long_name (from routes.txt), "headSign":
 * trip_headsign (from trips.txt) }
 */
public class RouteInfo {

    private final String operatorNameString; // agency_name
    private final String shortNameString;    // route_short_name
    private final String longNameString;     // route_long_name
    private final String headSignString;     // trip_headsign

    /**
     * Constructor for RouteInfo.
     *
     * @param operatorNameString the name of the operator (agency_name)
     * @param shortNameString the short name of the route (route_short_name)
     * @param longNameString the long name of the route (route_long_name)
     * @param headSignString the head sign of the trip (trip_headsign)
     */
    public RouteInfo(String operatorNameString, String shortNameString, String longNameString, String headSignString) {
        this.operatorNameString = operatorNameString;
        this.shortNameString = shortNameString;
        this.longNameString = longNameString;
        this.headSignString = headSignString;
    }

    // (Optional) Add getters if you need to serialize to JSON or inspect fields elsewhere:
    public String getOperatorNameString() {
        return operatorNameString;
    }

    public String getShortNameString() {
        return shortNameString;
    }

    public String getLongNameString() {
        return longNameString;
    }

    public String getHeadSignString() {
        return headSignString;
    }

    public String toString(){
        return String.format("{\"operator\":\"%s\",\"shortName\":\"%s\",\"longName\":\"%s\",\"headSign\":%s}",
                                 this.operatorNameString, this.shortNameString, this.longNameString, this.headSignString);
    }
}
