package routing.routingEngineDijkstra.dijkstra.model.input;

//what could possibly go wrong with having public attributes he asked

/**
 * Represents information about a route, including operator, short name, long name, and head sign.
 */
public class DijkstraRouteInfo {
    /** The operator of the route. */
    public final String operator;
    /** The short name or code of the route. */
    public final String shortName;
    /** The full descriptive name of the route. */
    public final String longName;
    /** The destination sign displayed on the vehicle. */
    public final String headSign;

    /**
     * Constructs a DijkstraRouteInfo with the specified route details.
     *
     * @param operator   the operator of the route
     * @param shortName  the short name or code of the route
     * @param longName   the full descriptive name of the route
     * @param headSign   the destination sign displayed on the vehicle
     */
    public DijkstraRouteInfo(String operator, String shortName, String longName, String headSign) {
        this.operator = operator;
        this.shortName = shortName;
        this.longName = longName;
        this.headSign = headSign;
    }
}