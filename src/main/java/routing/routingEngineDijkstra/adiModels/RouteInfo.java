package routing.routingEngineDijkstra.adiModels;

public class RouteInfo {
    private final String operator;
    private final String shortName;
    private final String longName;
    private final String headSign;

    public RouteInfo(String operator, String shortName, String longName, String headSign) {
        this.operator = operator;
        this.shortName = shortName;
        this.longName = longName;
        this.headSign = headSign;
    }

    public String getOperator() { return operator; }
    public String getShortName() { return shortName; }
    public String getLongName() { return longName; }
    public String getHeadSign() { return headSign; }
}