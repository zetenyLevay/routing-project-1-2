package closureAnalysis.data.enums;

/**
 * Enum representing different types of transportation with associated metrics.
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
     * Constructs a TransportType with the specified properties.
     * @param intType The integer type identifier
     * @param yearlyPassengers Yearly passengers in millions
     * @param stopCount Count of stops for this transport type
     */

    TransportType(int intType, double yearlyPassengers, double stopCount) {
        this.intType = intType;
        this.yearlyPassengers = yearlyPassengers;
        this.stopCount = stopCount;
    }
    /**
     * Gets the integer type identifier for this transport type.
     * @return The integer type identifier
     */
    public int getIntType() {
        return intType;
    }
    /**
     * Retrieves the TransportType corresponding to the given integer type.
     * @param value The integer type to look up
     * @return The matching TransportType
     * @throws IllegalArgumentException if no TransportType matches the given integer
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
