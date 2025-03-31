package ClosureAnalysis.Data.Enums;
public enum POIType {
    SIGHTS(10),
    COMMERCIAL(15),
    FOOD_SERVICE(20),
    SERVICE(20),
    HEALTHCARE(50),
    FINANCIAL(35),
    GREEN_SPACE(15),
    EDUCATION(50),
    MISC(10);

    public final int value;

    POIType(int value) {
        this.value = value;
    }
}
