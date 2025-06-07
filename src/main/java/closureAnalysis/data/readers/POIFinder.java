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

public class POIFinder implements Finder {

    private final double BIGRADIUS = 800;
    private final double SMALLRADIUS = 400;

    private final DistanceCalculator calculator = new DistanceCalculator();
    // private List<PointOfInterest> allPOIs = new ArrayList<>();

    private KDTree poiTree = new KDTree();

    /**
     * we preload all the Points of interests, since we dont want to query again and again for each stop
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
     * using kdTree (k dimensional tree) in our case k is 2, we find all pois nearby
     * @param stop
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
