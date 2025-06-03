package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;
import routing.routingEngineModels.Coordinates;

import java.sql.*;

public class CoordinateFinder implements Finder {
    @Override
    public void find(StopNode input) {

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/june2ndBudapestGTFS.db");
            String query = "select stop_lat, stop_lon\n" +
                    "from stops \n" +
                    "where stop_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, input.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                double latitude = rs.getDouble("stop_lat");
                double longitude = rs.getDouble("stop_lon");

                Coordinates coordinates = new Coordinates(latitude, longitude);

                input.setCoordinates(coordinates);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
