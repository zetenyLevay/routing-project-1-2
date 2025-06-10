package closureAnalysis.data.readers;


import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.graph.StopNode;
import closureAnalysis.data.enums.POIType;
import closureAnalysis.data.models.NearbyPOIs;
import closureAnalysis.data.models.PointOfInterest;
import closureAnalysis.kdtree.KDTree;
import routing.routingEngineModels.Coordinates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Finds Points of Interest (POIs) near transit stops using a KD-Tree for spatial indexing.
 *
 * <p>This implementation:
 * <ul>
 *   <li>Preloads all POIs from a spatial database into a KD-Tree for efficient range queries</li>
 *   <li>Categorizes POIs into "close" (within 400m) and "far" (400-800m) groups</li>
 *   <li>Uses efficient spatial searches to minimize computation time</li>
 * </ul>
 */
@SuppressWarnings("all")
public class POIFinder implements Finder {

    @SuppressWarnings("FieldCanBeLocal")
    private final double BIGRADIUS = 800;
    @SuppressWarnings("FieldCanBeLocal")
    private final double SMALLRADIUS = 400;

    private final DistanceCalculator calculator = new DistanceCalculator();

    private final KDTree poiTree = new KDTree();

    /**
     * Preloads all Points of Interest from the database into a KD-Tree structure.
     * Reads POI data including location coordinates and category types.
     */
    public void preload() {
        String query = "SELECT ogc_fid, category, xcoord, ycoord FROM added_geom_info";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/ClosureAnalysis/countyBuildingsAsPoints.sqlite");
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("ogc_fid");
                String type = rs.getString("category");
                double lon = rs.getDouble("xcoord");
                double lat = rs.getDouble("ycoord");



                Coordinates coords = new Coordinates(lat, lon);
                PointOfInterest poi = new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), coords);
                poiTree.insert(poi);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    /**
     * Finds nearby POIs for a stop and categorizes them into close and far groups.
     * @param stop The StopNode to find POIs around
     */
     public void find(StopNode stop) {


         Coordinates stopCoords = stop.getCoordinates();

         Set<PointOfInterest> closePOIsSet = new HashSet<>(poiTree.rangeSearch(stopCoords, SMALLRADIUS, calculator));
         List<PointOfInterest> closePOIs = new ArrayList<>(closePOIsSet);
         List<PointOfInterest> farPOIs = new ArrayList<>();
         for (PointOfInterest poi : poiTree.rangeSearch(stopCoords, BIGRADIUS, calculator)) {
             if (!closePOIsSet.contains(poi)) {
                 farPOIs.add(poi);
             }
         }

         stop.setNearbyPOIs(new NearbyPOIs( closePOIs, farPOIs));
    }
}
