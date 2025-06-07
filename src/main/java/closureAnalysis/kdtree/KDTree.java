package closureAnalysis.kdtree;

import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.models.PointOfInterest;
import routing.routingEngineModels.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * Using kd tree in order to improve POI finding speed
 * this is a binary tree, where we switch between comparing the x coordinates and y coordinates
 * if more understanding needed i suggest <a href="https://www.youtube.com/watch?v=Glp7THUpGow">this</a>
 */
public class KDTree {

    private final int dimensions = 2;
    private class KDNode {
        PointOfInterest poi;
        KDNode left, right;
        boolean isVertical; // this is to check if we need to look at latitude or longitude (it alternates between true and false, so that we spli
        KDNode(PointOfInterest poi, boolean vertical) {
            this.poi = poi;
            this.isVertical = vertical ;
            this.left = this.right = null;
        }
    }
    private KDNode root;

    /**
     * default insert if KDTree is not started yet
     * @param poi
     */
    public void insert(PointOfInterest poi) {
        root = insert(root, poi, true);
    }

    /**
     *
     * @param node parent node
     * @param poi what we are inserting
     * @param isVertical if yes, we are checking x coordinate, if no we are checking y coordinate
     * @return
     */
    private KDNode insert(KDNode node, PointOfInterest poi, boolean isVertical) {
        if (node == null) return new KDNode(poi, isVertical);

        // if vertical, take latitude (x), if not take longitude(y)
        double comparisonValue1 = isVertical ? poi.getCoordinates().getLatitude() : poi.getCoordinates().getLongitude();
        double comparisonValue2 = isVertical ? node.poi.getCoordinates().getLatitude() : node.poi.getCoordinates().getLongitude();

        if (comparisonValue1 < comparisonValue2) {
            node.left = insert(node.left, poi, !isVertical);
        } else {
            node.right = insert(node.right, poi, !isVertical);
        }
        return node;
    }

    /**
     * starts the search
     * @param center the stop's coordinates that we are looking around
     * @param radiusMeters how far we are looking
     * @param calculator
     * @return every point of interest in the RadiusMeters
     */
    public List<PointOfInterest> rangeSearch(Coordinates center, double radiusMeters, DistanceCalculator calculator) {
        List<PointOfInterest> result = new ArrayList<>();
        rangeSearch(root, center, radiusMeters, calculator, result);
        return result;
    }

    /**
     *
     * @param node current POI we are checking
     * @param center start point
     * @param radiusMeters radius to check
     * @param calculator
     * @param result all pois
     */

    private void rangeSearch(KDNode node, Coordinates center, double radiusMeters, DistanceCalculator calculator, List<PointOfInterest> result) {
        if (node == null) return;

        // check if poi is inside radius
        double distance = calculator.calculateDistance(center, node.poi.getCoordinates());
        if (distance <= radiusMeters) {
            result.add(node.poi);
        }

        // check where we should search next
        double compValCenter = node.isVertical ? center.getLatitude() : center.getLongitude();
        double compValNode = node.isVertical ? node.poi.getCoordinates().getLatitude() : node.poi.getCoordinates().getLongitude();

        // we divide by 111 000 because 1 degree is about 111km (so 111 000 meters) and we are trying to compare degrees
        if (compValCenter - radiusMeters / 111000.0 < compValNode) {
            rangeSearch(node.left, center, radiusMeters, calculator, result);
        }
        if (compValCenter + radiusMeters / 111000.0 >= compValNode) {
            rangeSearch(node.right, center, radiusMeters, calculator, result);
        }
    }
}
