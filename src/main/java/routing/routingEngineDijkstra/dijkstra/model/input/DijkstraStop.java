package routing.routingEngineDijkstra.dijkstra.model.input;

//ashish is gonna kill me if he sees this

/**
 * Represents a stop in the routing system, including its identifier, name, and geographical coordinates.
 */
public class DijkstraStop {
    /** The unique identifier of the stop. */
    public final String id;
    /** The name of the stop. */
    public final String name;
    /** The latitude of the stop's location. */
    public final double lat;
    /** The longitude of the stop's location. */
    public final double lon;

    /**
     * Constructs a DijkstraStop with the specified details.
     *
     * @param id   the unique identifier of the stop
     * @param name the name of the stop
     * @param lat  the latitude of the stop's location
     * @param lon  the longitude of the stop's location
     */
    public DijkstraStop(String id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
}