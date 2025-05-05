package closureAnalysis.data.readers;

import closureAnalysis.calculations.DistanceCalculator;
import closureAnalysis.data.models.RoadSegment;
import closureAnalysis.data.models.Stop;
import routing.routingEngineModels.csamodel.Coordinates;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StopFinder implements Finder<RoadSegment>{

    DistanceCalculator calculator = new DistanceCalculator();
   @Override
    public void find(RoadSegment roadSegment) {

        String query = "SELECT stop_id, stop_name ,stop_lat, stop_lon FROM stops";

        List<Stop> stopsOnRoadSegment = new ArrayList<Stop>();

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:data/ClosureAnalysis/stops.sqlite");
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery())
        {
            while (resultSet.next()) {
                String id = resultSet.getString("stop_id");
                String name = resultSet.getString("stop_name");
                double lat = resultSet.getDouble("stop_lat");
                double lon = resultSet.getDouble("stop_lon");
                Coordinates coordinates = new Coordinates(lat, lon);

                Coordinates test1 = new Coordinates(47.501104, 19.082712);
                Coordinates test2 = new Coordinates(47.500853, 19.082181);

                double distanceFromRoadSegment = calculator.stopToRoadSegmentDistance(test1, test2, coordinates ) ;



                if ( distanceFromRoadSegment < 1 && distanceFromRoadSegment > 0 ) {
                    System.out.println(id + " " + name + " " + lat + " " + lon);
                    stopsOnRoadSegment.add(new Stop(id, name, coordinates));
                }
            }


        }
        catch (Exception e) {

        }

        roadSegment.setStopList(stopsOnRoadSegment);

    }

    public static void main(String[] args) {

        Finder<RoadSegment> finder = new StopFinder();

        RoadSegment roadSegment = new RoadSegment("1",
                new Coordinates(47.5009585313839, 19.0826709483695),
                new Coordinates(47.5013360673781, 19.0834875092641));

        finder.find(roadSegment);

        System.out.println(roadSegment.getStopList().size());

    }
}
