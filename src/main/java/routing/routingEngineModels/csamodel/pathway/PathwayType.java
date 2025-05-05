package routing.routingEngineModels.csamodel.pathway;

public enum PathwayType {
    WALKWAY(1, "Walkway"),
    STAIRS(2, "Stairs"),
    MOVING_SIDEWALK(3, "Moving Sidewalk/Travelator"),
    ESCALATOR(4, "Escalator"),
    ELEVATOR(5, "Elevator"),
    FARE_GATE(6, "Fare Gate"),
    EXIT_GATE(7, "Exit Gate"),
    UNDEFINED(-1, "UNDEFINED");

    private final int code;
    private final String name;

    PathwayType(int code, String displayName) {
        this.code = code;
        this.name = displayName;
    }

    public static PathwayType getNameFromCode(int code) {
        for (PathwayType pathwayType : PathwayType.values()) {
            if (pathwayType.code == code) {
                return pathwayType;
            }
        }
        return UNDEFINED;
    }

    public String showDisplayName() {
        return name;
    }
}
