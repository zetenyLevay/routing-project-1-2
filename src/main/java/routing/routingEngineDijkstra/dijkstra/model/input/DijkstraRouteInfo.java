package routing.routingEngineDijkstra.dijkstra.model.input;

public class DijkstraRouteInfo {
    public final String operator;
    public final String shortName;
    public final String longName;
    public final String headSign;

    public DijkstraRouteInfo(String operator, String shortName, String longName, String headSign) {
        this.operator = operator;
        this.shortName = shortName;
        this.longName = longName;
        this.headSign = headSign;
    }
}
//what could possibly go wrong with having public attributes he asked