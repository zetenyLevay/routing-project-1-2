package ClosureAnalysis.Data.Enums;
public enum POIType {
    AGRICULTURAL(10),
    COMMERCIAL(15),
    EDUCATION(20),
    HEALTHCARE(20),
    HOSPITALITY(50),
    OTHERSTRUCTURES(35),
    PUBLICSERVICES(15),
    RELIGIOUS(50),
    RESIDENTIALHOUSING(10),
    TRANSPORTATION(0),
    ENTERTAINMENT(0),
    INDUSTRIAL(0),
    INFRASTRUCTURE(0),
    RECREATION(0);

    public final int value;

    POIType(int value) {
        this.value = value;
    }
}
