package ClosureAnalysis.Calculations;


import ClosureAnalysis.Data.Models.PointOfInterest;
import ClosureAnalysis.Data.Models.Stop;
import ClosureAnalysis.Data.Readers.POIReader;
import ClosureAnalysis.Data.Readers.Reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class POIFinder {
    public List<PointOfInterest> findPOIsInRadius(Stop stop, List<PointOfInterest> pointsOfInterest, double radiusKm ) {
        List<PointOfInterest> result = new ArrayList<PointOfInterest>();

        for (PointOfInterest pointOfInterest : pointsOfInterest) {
            double distance = DistanceCalculator.calculateDistance(stop.getLatitude(), stop.getLongitude(),
                    pointOfInterest.getLatitude(), pointOfInterest.getLongitude());

            if (distance <= radiusKm) {
                result.add(pointOfInterest);
            }
        }
        return result;
    }
    public static void main(String[] args) throws IOException {
        POIReader reader = new POIReader("data/ClosureAnalysis/POI_data.geojson");
        POIFinder finder = new POIFinder();
        reader.readFile();
        reader.parseData();
        List<PointOfInterest> pointOfInterests = reader.getPointOfInterests();

        List<Double> testCoords = new ArrayList<>();
        testCoords.add(19.175432);
        testCoords.add(47.428600);

        Stop testStop = new Stop("1", "Stop", testCoords, 0, null, 0,0);

        List<PointOfInterest> result = finder.findPOIsInRadius(testStop, pointOfInterests, 0.5);

        for (PointOfInterest pointOfInterest : result) {
            System.out.println(pointOfInterest.getType());
        }

    }
}
