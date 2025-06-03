package routing.routingEngineDijkstra.dijkstra;

public enum StopTypeDijkstra {
    STOP(0),           // Regular stop
    STATION(1),        // Station
    ENTRANCE_EXIT(2),  // Entrance or exit
    GENERIC_NODE(3),   // Generic node
    BOARDING_AREA(4),  // Boarding area
    PLATFORM(5);       // Specific platform (used for boarding)

    private final int code;

    StopTypeDijkstra(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StopTypeDijkstra fromCode(int code) {
        for (StopTypeDijkstra type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return STOP; // default fallback
    }
}