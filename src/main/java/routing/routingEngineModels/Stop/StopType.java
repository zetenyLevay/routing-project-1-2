package routing.routingEngineModels.Stop;

public enum StopType {
    STOP(0, "Stop/Platform"),
    STATION(1, "Station"),
    ENTRANCE_EXIT(2, "Entrance/Exit"),
    GENERIC_NODE(3, "Generic Node"),
    BOARDING_AREA(4, "Boarding Area"),
    UNDEFINED(-1, "Undefined");

    private final int code;
    private final String displayName;

    StopType(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StopType getNameFromCode(Integer code) {
        if (code == null || code == 0) return STOP;
        for (StopType type : values()) {
            if (type.code == code) return type;
        }
        return UNDEFINED;
    }

    public boolean isPlatform(String parentStationId) {
        return this == STOP && parentStationId != null;
    }
}