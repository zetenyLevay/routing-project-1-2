package closureAnalysis.data.enums;

/**
 * TransportType.java
 *
 * Enum representing different types of public transport.
 * Each type has an associated integer type, yearly passenger count (in millions),
 * and the number of stops of that type.
 */
public enum TransportType {
    BUS(3, 564, 4462),
    TRAM(0, 408, 657),
    METRO(1, 385.6, 103 ),
    TROLLEY(11, 79.7, 331),
    BOAT(4, 0.07, 2),
    SUBURBAN_RAIL(109, 59.4, 140);

    public final int intType;
    public final double yearlyPassengers; // millions
    public final double stopCount; // how many of these types exist

    /**
     * Constructor for TransportType enum.
     *
     * @param intType the integer type representing the transport type
     * @param yearlyPassengers the estimated number of passengers per year (in millions)
     * @param stopCount the number of stops of this transport type
     */
    TransportType(int intType, double yearlyPassengers, double stopCount) {
        this.intType = intType;
        this.yearlyPassengers = yearlyPassengers;
        this.stopCount = stopCount;
    }

    /**
     * Gets the integer type of the transport type.
     *
     * @return the integer type
     */
    public int getIntType() {
        return intType;
    }
    /**
     * Gets the estimated number of passengers per year (in millions).
     *
     * @return the yearly passenger count
     */
    public static TransportType fromIntType(int value) {
        for (TransportType type : values()) {
            if (type.getIntType() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GTFS route_type: " + value);
    }
}
