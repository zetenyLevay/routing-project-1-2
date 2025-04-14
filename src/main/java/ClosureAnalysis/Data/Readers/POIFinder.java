package ClosureAnalysis.Data.Readers;


import ClosureAnalysis.Calculations.DistanceCalculator;
import ClosureAnalysis.Data.Enums.POIType;
import ClosureAnalysis.Data.Models.NearbyPOIs;
import ClosureAnalysis.Data.Models.PointOfInterest;
import ClosureAnalysis.Data.Models.Stop;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class POIFinder {

    private final double BIGRADIUS = 0.8;
    private final double SMALLRADIUS = 0.4;

    private final DistanceCalculator calculator = new DistanceCalculator();
     public NearbyPOIs findPOIs(Stop stop) {

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

                boolean inSmallRadius = calculator.calculateDistance(stop.getLatitude(), stop.getLongitude(), latitude, longitude) <= SMALLRADIUS;
                boolean inBigRadius = calculator.calculateDistance(stop.getLatitude(), stop.getLongitude(), latitude, longitude) <= BIGRADIUS;


                if (inSmallRadius) {
                    closePOIs.add(new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), List.of(longitude, latitude)));
                } else if (inBigRadius && !inSmallRadius) {
                    farPOIs.add(new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), List.of(longitude, latitude)));
                }


            }
        }
        catch (Exception e) {
            System.out.println("shhit is fucked");
            e.printStackTrace();

        }

        NearbyPOIs result = new NearbyPOIs(closePOIs, farPOIs);

        return result;
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        POIFinder finder = new POIFinder();
        Stop testStop = new Stop("1", "a", List.of(47.510571,19.056072),0,null,0,0);

        NearbyPOIs poisInRadius = finder.findPOIs(testStop);

        System.out.println(poisInRadius.getClosePointsOfInterest().size());
        System.out.println(poisInRadius.getFarPointOfInterest().size());

    }
}
