package routing.routingEngineModels;

import java.util.Map;

/**
 * RouteInfo.java
 *
 * Represents information about a route, including the operator name, short name,
 * long name, and head sign. This class is used to encapsulate route details for
 * display or processing in routing applications.
 */
public class RouteInfo {
    private final String operatorNameString; // agency_name
    private final String shortNameString;    // route_short_name
    private final String longNameString;     // route_long_name
    private final String headSignString;     // trip_headsign

    /**
     * Constructor for RouteInfo.
     *
     * @param operatorNameString The name of the operator (agency).
     * @param shortNameString The short name of the route.
     * @param longNameString The long name of the route.
     * @param headSignString The head sign of the trip.
     */
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

    // Getters for the route information
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

    /**
     * Returns a string representation of the RouteInfo object in a JSON-like format.
     * This is useful for debugging or logging purposes.
     *
     * @return A string representation of the RouteInfo object.
     */
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
     * Converts the RouteInfo object to a JSON-like Map representation.
     * This is useful for serialization or API responses.
     *
     * @return A Map representing the RouteInfo in JSON format.
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
