package routing.routingEngineDijkstra.adiModels;

/**
 * Represents information about a route, including operator, short name, long name, and head sign.
 */
public class AdiRouteInfo {
    private final String operator;
    private final String shortName;
    private final String longName;
    private final String headSign;

    /**
     * Constructs an AdiRouteInfo with the specified route details.
     *
     * @param operator   the operator of the route
     * @param shortName  the short name or code of the route
     * @param longName   the full descriptive name of the route
     * @param headSign   the destination sign displayed on the vehicle
     */
    public AdiRouteInfo(String operator, String shortName, String longName, String headSign) {
        this.operator = operator;
        this.shortName = shortName;
        this.longName = longName;
        this.headSign = headSign;
    }

    /**
     * Retrieves the operator of the route.
     *
     * @return the operator as a String
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Retrieves the short name or code of the route.
     *
     * @return the short name as a String
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Retrieves the full descriptive name of the route.
     *
     * @return the long name as a String
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Retrieves the head sign of the route.
     *
     * @return the head sign as a String
     */
    public String getHeadSign() {
        return headSign;
    }
}