package closureAnalysis.data.readers;


import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.enums.POIType;
import closureAnalysis.data.models.NearbyPOIs;
import closureAnalysis.data.models.PointOfInterest;
import closureAnalysis.data.models.Stop;
import routing.routingEngineModels.csamodel.Coordinates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class POIFinder implements Finder<Stop> {

    private final double BIGRADIUS = 0.8;
    private final double SMALLRADIUS = 0.4;

    private final DistanceCalculator calculator = new DistanceCalculator();
     public void find(Stop stop) {

        String query = "SELECT fid, buildingcategory, longitude, latitude FROM buildingsaspoints";
        List<PointOfInterest> closePOIs = new ArrayList<>();
        List<PointOfInterest> farPOIs = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/ClosureAnalysis/BuildingsAsPoints.sqlite");
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {

                String id = rs.getString("fid");
                String type = rs.getString("buildingcategory");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");

                boolean inSmallRadius = calculator.calculateDistance(stop.getCoordinates(), new Coordinates(latitude, longitude)) <= SMALLRADIUS;
                boolean inBigRadius = calculator.calculateDistance(stop.getCoordinates(), new Coordinates(latitude, longitude)) <= BIGRADIUS;


                if (inSmallRadius) {
                    closePOIs.add(new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), new Coordinates(latitude, longitude)));
                } else if (inBigRadius) {
                    farPOIs.add(new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), new Coordinates(latitude, longitude)));
                }


            }
        }
        catch (Exception e) {
            System.out.println("shhit is fucked");
            e.printStackTrace();

        }

        stop.setNearbyPOIs(new NearbyPOIs(closePOIs, farPOIs));


    }
    public static void main(String[] args) {
        POIFinder finder = new POIFinder();
        Stop testStop = new Stop("1", "a", new Coordinates(47.510571,19.056072));

        finder.find(testStop);

        System.out.println(testStop.getNearbyPOIs());



    }
}
