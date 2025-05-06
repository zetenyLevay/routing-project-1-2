package routing.routingEngineCSA.engine.cache.classloader;

import routing.routingEngineModels.csamodel.pathway.Pathway;
import routing.routingEngineModels.Stop;

import java.sql.*;

public class PathwaysCache {

    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite::resource:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pathways");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("pathway_id");
                String fromId = rs.getString("from_stop_id");
                String toId = rs.getString("to_stop_id");
                int type = rs.getInt("pathway_mode");
                int time = rs.getInt("traversal_time");

                Stop from = StopsCache.getStop(fromId);
                Stop to = StopsCache.getStop(toId);

                if (from != null && to != null) {
                    Pathway pathway = new Pathway(id, from, to, type, time);
                    from.getFootpaths().add(pathway);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
