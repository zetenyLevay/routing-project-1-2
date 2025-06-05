package routing.routingEngineDijkstra.dijkstra.model.input;

public class DijkstraStop {
    public final String id;
    public final String name;
    public final double lat;
    public final double lon;

    public DijkstraStop(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}
//ashish is gonna kill me if he sees this