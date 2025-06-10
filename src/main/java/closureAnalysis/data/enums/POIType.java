package closureAnalysis.data.enums;
/**
 * Enum representing different types of Points of Interest (POI) with associated values.
 */
public enum POIType {
    AGRICULTURAL(5),
    COMMERCIAL(20),
    EDUCATION(30),
    HEALTHCARE(50),
    HOSPITALITY(15),
    OTHERSTRUCTURES(1),
    PUBLICSERVICES(20),
    RELIGIOUS(30),
    RESIDENTIALHOUSING(5),
    TRANSPORTATION(5),
    ENTERTAINMENT(10),
    INDUSTRIAL(5),
    INFRASTRUCTURE(5),
    RECREATION(5),
    NONBUILDING(0);

    public final int value;

    POIType(int value) {
        this.value = value;
    }
}
