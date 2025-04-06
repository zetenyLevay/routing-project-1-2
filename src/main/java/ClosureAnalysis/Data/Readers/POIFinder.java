package ClosureAnalysis.Data.Readers;


import ClosureAnalysis.Calculations.DistanceCalculator;
import ClosureAnalysis.Data.Enums.POIType;
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

    private final DistanceCalculator calculator = new DistanceCalculator();
     public  List<PointOfInterest> findPOIs(Stop stop, double radius) {

        String query = "SELECT fid, buildingcategory, longitude, latitude FROM buildingsaspoints";
        List<PointOfInterest> results = new ArrayList<>();


        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/ClosureAnalysis/BuildingsAsPoints.sqlite");
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {

                String id = rs.getString("fid");
                String type = rs.getString("buildingcategory");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");


                if (calculator.calculateDistance(stop.getLatitude(), stop.getLongitude(), latitude, longitude) <= radius) {
                    results.add(new PointOfInterest(id, POIType.valueOf(type.toUpperCase()), List.of(longitude, latitude)));
                }


            }
        }
        catch (Exception e) {
            System.out.println("shhit is fucked");
            e.printStackTrace();

        }
        return results;
    }
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        POIFinder finder = new POIFinder();
        Stop testStop = new Stop("1", "a", List.of(47.510571,19.056072),0,null,0,0);

        List<PointOfInterest> result = finder.findPOIs(testStop, 0.4);

        System.out.println(result.size());
    }
}
