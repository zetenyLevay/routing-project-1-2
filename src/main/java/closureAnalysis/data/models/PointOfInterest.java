package closureAnalysis.data.models;

import closureAnalysis.data.enums.POIType;
import routing.routingEngineModels.Coordinates;

/**
 * Represents a geographic point of interest with categorical classification.
 *
 * <p>Each POI contains:
 * <ul>
 *   <li>A unique identifier</li>
 *   <li>A classification type (from POIType enum)</li>
 *   <li>Geographic coordinates</li>
 * </ul>
 *
 * <p>Used to evaluate the amenities and services available near transit stops.
 */
public class PointOfInterest {
    /**
     * Unique identifier for the POI
     */
    private String id;
    /**
     * Classification category of the POI
     */
    private POIType type;

    /**
     * Geographic location of the POI
     */
    private Coordinates coordinates;
    /**
     * Creates a new PointOfInterest instance.
     * @param id Unique identifier
     * @param type Classification category
     * @param coordinates Geographic location
     */
    public PointOfInterest(String id, POIType type, Coordinates coordinates) {
        this.id = id;
        this.type = type;
        this.coordinates = coordinates;
    }

    // Standard getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public POIType getType() {
        return type;
    }

    public void setType(POIType type) {
        this.type = type;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public double getLatitude(){
        return coordinates.getLatitude();
    }

    public double getLongitude(){
        return coordinates.getLongitude();
    }
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
}
