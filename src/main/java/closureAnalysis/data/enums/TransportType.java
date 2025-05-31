package closureAnalysis.data.enums;

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

    TransportType(int intType, double yearlyPassengers, double stopCount) {
        this.intType = intType;
        this.yearlyPassengers = yearlyPassengers;
        this.stopCount = stopCount;
    }

    public int getIntType() {
        return intType;
    }

    public static TransportType fromIntType(int value) {
        for (TransportType type : values()) {
            if (type.getIntType() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown GTFS route_type: " + value);
    }
}
