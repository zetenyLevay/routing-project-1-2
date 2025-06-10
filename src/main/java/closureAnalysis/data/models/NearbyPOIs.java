package closureAnalysis.data.models;

import java.util.List;

/**
 * Represents categorized Points of Interest (POIs) near a transit stop.
 *
 * <p>This class provides structured access to POIs divided into two distance-based categories:
 * <ul>
 *   <li><b>Close POIs</b>: Points of interest within immediate walking distance (typically 400m)</li>
 *   <li><b>Far POIs</b>: Points of interest within moderate walking distance (typically 400-800m)</li>
 * </ul>
 *
 * <p>Used to evaluate a stop's importance based on nearby amenities and services.
 */
public class NearbyPOIs {
    /**
     * List of POIs close to the stop
     */
    List<PointOfInterest> closePointsOfInterest;
    /**
     * List of POIs within moderate distance from the stop
     */
    List<PointOfInterest> farPointsOfInterest;

    /**
     * Creates a new NearbyPOIs instance with categorized POI lists.
     * @param closePointsOfInterest POIs within immediate walking distance
     * @param farPointsOfInterest POIs within moderate walking distance
     */
    public NearbyPOIs(List<PointOfInterest> closePointsOfInterest, List<PointOfInterest> farPointsOfInterest) {
        this.closePointsOfInterest = closePointsOfInterest;
        this.farPointsOfInterest = farPointsOfInterest;
    }

    // Getter and setter methods
    public List<PointOfInterest> getClosePointsOfInterest() {
        return closePointsOfInterest;
    }
    public List<PointOfInterest> getFarPointOfInterest() {
        return farPointsOfInterest;
    }

}
