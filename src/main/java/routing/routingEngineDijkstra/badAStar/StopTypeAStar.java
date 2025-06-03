package routing.routingEngineDijkstra.badAStar;

public enum StopTypeAStar {
    STOP(0),           // Regular stop
    STATION(1),        // Station
    ENTRANCE_EXIT(2),  // Entrance or exit
    GENERIC_NODE(3),   // Generic node
    BOARDING_AREA(4),  // Boarding area
    PLATFORM(5);       // Specific platform (used for boarding)

    private final int code;

    StopTypeAStar(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StopTypeAStar fromCode(int code) {
        for (StopTypeAStar type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return STOP; // default fallback
    }
}
