package routing.routingEngineModels;

import java.util.Map;

public class RouteInfo {
    private final String operatorNameString; // agency_name
    private final String shortNameString;    // route_short_name
    private final String longNameString;     // route_long_name
    private final String headSignString;     // trip_headsign

    public RouteInfo(
        String operatorNameString,
        String shortNameString,
        String longNameString,
        String headSignString
    ) {
        this.operatorNameString = operatorNameString;
        this.shortNameString    = shortNameString;
        this.longNameString     = longNameString;
        this.headSignString     = headSignString;
    }

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

    @Override
    public String toString() {
        // This returns a JSON‐like String. If you put this directly into json.put("route", …),
        // it will be double‐quoted and escaped by JSONWriter.
        return String.format(
            "{\"operator\":\"%s\",\"shortName\":\"%s\",\"longName\":\"%s\",\"headSign\":\"%s\"}",
            operatorNameString,
            shortNameString,
            longNameString,
            headSignString
        );
    }

    /**
     * Return a Map so that JSONWriter emits a true nested object:
     *   "route": { "operator":"BKK", "shortName":"5", … }
     * instead of a quoted, escaped String.
     */
    public Map<String, Object> toJSON() {
        return Map.of(
            "operator", operatorNameString,
            "shortName", shortNameString,
            "longName", longNameString,
            "headSign", headSignString
        );
    }
}
