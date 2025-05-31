package closureAnalysis.data.enums;
public enum POIType {
    AGRICULTURAL(5),
    COMMERCIAL(10),
    EDUCATION(30),
    HEALTHCARE(50),
    HOSPITALITY(15),
    OTHERSTRUCTURES(5),
    PUBLICSERVICES(15),
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
