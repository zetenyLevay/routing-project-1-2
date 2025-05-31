package closureAnalysis.kdtree;

import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.models.PointOfInterest;
import routing.routingEngineModels.Coordinates;

import java.util.ArrayList;
import java.util.List;

public class KDTree {

    private final int dimensions = 2;
    private class KDNode {
        PointOfInterest poi;
        KDNode left, right;

        boolean isVertical; // this is to check if we need to look at latitude or longitude


        KDNode(PointOfInterest poi, boolean vertical) {
            this.poi = poi;
            this.isVertical = vertical ;
            this.left = this.right = null;
        }
    }

    private KDNode root;

    public void insert(PointOfInterest poi) {
        root = insert(root, poi, true);
    }

    private KDNode insert(KDNode node, PointOfInterest poi, boolean isVertical) {
        if (node == null) return new KDNode(poi, isVertical);

        double comparisonValue1 = isVertical ? poi.getCoordinates().getLatitude() : poi.getCoordinates().getLongitude();
        double comparisonValue2 = isVertical ? node.poi.getCoordinates().getLatitude() : node.poi.getCoordinates().getLongitude();

        if (comparisonValue1 < comparisonValue2) {
            node.left = insert(node.left, poi, !isVertical);
        } else {
            node.right = insert(node.right, poi, !isVertical);
        }
        return node;
    }

    public List<PointOfInterest> rangeSearch(Coordinates center, double radiusMeters, DistanceCalculator calculator) {
        List<PointOfInterest> result = new ArrayList<>();
        rangeSearch(root, center, radiusMeters, calculator, result);
        return result;
    }

    private void rangeSearch(KDNode node, Coordinates center, double radiusMeters, DistanceCalculator calculator, List<PointOfInterest> result) {
        if (node == null) return;

        double distance = calculator.calculateDistance(center, node.poi.getCoordinates());
        if (distance <= radiusMeters) {
            result.add(node.poi);
        }

        double compValCenter = node.isVertical ? center.getLatitude() : center.getLongitude();
        double compValNode = node.isVertical ? node.poi.getCoordinates().getLatitude() : node.poi.getCoordinates().getLongitude();

        if (compValCenter - radiusMeters / 111000.0 < compValNode) {
            rangeSearch(node.left, center, radiusMeters, calculator, result);
        }
        if (compValCenter + radiusMeters / 111000.0 >= compValNode) {
            rangeSearch(node.right, center, radiusMeters, calculator, result);
        }
    }
}
