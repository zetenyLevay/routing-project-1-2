package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;
import routing.routingEngineModels.Coordinates;

import java.sql.*;

/**
 * Retrieves geographic coordinates for transit stops from a GTFS database.
 *
 * <p>This implementation queries a SQLite database containing GTFS stop data to find
 * the latitude and longitude for a given stop ID. The coordinates are then attached
 * to the StopNode object.
 *
 * <p>Separated from the main graph building process to keep the primary query efficient.
 */
public class CoordinateFinder implements Finder {
    /**
     * Finds and sets the geographic coordinates for the given stop node.
     * @param input The StopNode whose coordinates need to be found
     * @throws RuntimeException if a database error occurs during the query
     */
    @Override
    public void find(StopNode input) {

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/june2ndBudapestGTFS.db");
            String query = "select stop_lat, stop_lon " +
                    "from stops " +
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
