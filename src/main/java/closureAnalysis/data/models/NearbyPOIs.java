package closureAnalysis.data.models;

import java.util.List;

/**
 * This class is for ease of access of close and far points of interests
 */
public class NearbyPOIs {
    List<PointOfInterest> closePointsOfInterest;
    List<PointOfInterest> farPointsOfInterest;

    public NearbyPOIs(List<PointOfInterest> closePointsOfInterest, List<PointOfInterest> farPointsOfInterest) {
        this.closePointsOfInterest = closePointsOfInterest;
        this.farPointsOfInterest = farPointsOfInterest;
    }

    public List<PointOfInterest> getClosePointsOfInterest() {
        return closePointsOfInterest;
    }

    public void setClosePointsOfInterest(List<PointOfInterest> closePointsOfInterest) {
        this.closePointsOfInterest = closePointsOfInterest;
    }

    public List<PointOfInterest> getFarPointOfInterest() {
        return farPointsOfInterest;
    }

    public void setFarPointOfInterest(List<PointOfInterest> farPointOfInterest) {
        this.farPointsOfInterest = farPointOfInterest;
    }
}
