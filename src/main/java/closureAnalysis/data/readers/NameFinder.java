package closureAnalysis.data.readers;
import closureAnalysis.data.graph.StopNode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieves and attaches stop names to StopNodes using preloaded data from a GTFS database.
 *
 * <p>This implementation preloads all stop names into memory during initialization
 * for efficient lookup during the find operation. This avoids repeated database queries
 * when processing multiple stops.
 */
public class NameFinder implements Finder {

    Map<String, String> nameMap = new HashMap<>();

    /**
     * Preloads all stop names from the database into memory.
     * @param conn Active database connection to the GTFS data
     */
    public void preload(Connection conn) {
        String query = "SELECT stop_id, stop_name FROM stops";
        try (PreparedStatement ps = conn.prepareStatement(query)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String stopId = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");


                nameMap.putIfAbsent(stopId, stopName);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Finds and sets the name for the given stop node using preloaded data.
     * @param input The StopNode whose name needs to be set
     */
    @Override
    public void find(StopNode input) {
        String name = nameMap.get(input.getId());
        if (name != null) {
            input.setName(name);
        }
    }
}
