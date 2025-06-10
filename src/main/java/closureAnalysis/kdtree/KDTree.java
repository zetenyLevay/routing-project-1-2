package closureAnalysis.kdtree;

import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.models.PointOfInterest;
import routing.routingEngineModels.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * A k-dimensional tree (KD-Tree) implementation for efficient spatial searching of Points of Interest (POIs).
 *
 * <p>This data structure organizes geographic coordinates in a binary tree that alternates between
 * comparing latitude and longitude at each level, enabling efficient range queries and nearest neighbor searches.
 *
 * <p>Key features:
 * <ul>
 *   <li>Optimized for 2-dimensional geographic coordinates (latitude/longitude)</li>
 *   <li>Efficient range searches within a specified radius</li>
 *   <li>Balanced tree structure for logarithmic time complexity on average</li>
 *   <li>Supports dynamic insertion of new POIs</li>
 * </ul>
 *
 * <p>For more information on KD-Trees, see
 * <a href="https://www.youtube.com/watch?v=Glp7THUpGow">this explanatory video</a>.
 */
public class KDTree {
    /**
     * Represents a node in the KD-Tree containing a POI and references to child nodes.
     */
    private static class KDNode {
        PointOfInterest poi;
        KDNode left, right;
        boolean isVertical; // this is to check if we need to look at latitude or longitude
        /**
         * Creates a new KDNode with the specified POI and splitting orientation.
         * @param poi The Point of Interest to store
         * @param vertical The splitting orientation for this node
         */
        KDNode(PointOfInterest poi, boolean vertical) {
            this.poi = poi;
            this.isVertical = vertical ;
            this.left = this.right = null;
        }
    }
    /**
     * The root node of the KD-Tree
     */
    private KDNode root;

    /**
     * Inserts a new Point of Interest into the KD-Tree.
     * @param poi The Point of Interest to insert
     */
    public void insert(PointOfInterest poi) {
        root = insert(root, poi, true);
    }

    /**
     * Recursively inserts a POI into the KD-Tree.
     * @param node The current node being examined
     * @param poi The Point of Interest to insert
     * @param isVertical Whether to compare latitude (true) or longitude (false)
     * @return The updated node after insertion
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
     * Finds all Points of Interest within a specified radius of a center point.
     * @param center The central coordinates to search around
     * @param radiusMeters The search radius in meters
     * @param calculator The distance calculator to use
     * @return List of POIs within the specified radius
     */
    public List<PointOfInterest> rangeSearch(Coordinates center, double radiusMeters, DistanceCalculator calculator) {
        List<PointOfInterest> result = new ArrayList<>();
        rangeSearch(root, center, radiusMeters, calculator, result);
        return result;
    }
    /**
     * Recursively searches for POIs within a specified radius of a center point.
     * @param node The current node being examined
     * @param center The central coordinates to search around
     * @param radiusMeters The search radius in meters
     * @param calculator The distance calculator to use
     * @param result The accumulating list of found POIs
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
