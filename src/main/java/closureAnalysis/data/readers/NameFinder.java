package closureAnalysis.data.readers;

import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NameFinder implements Finder {

    Map<String, String> nameMap = new HashMap<String, String>();

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
     * @param input
     */
    @Override
    public void find(StopNode input) {
        String name = nameMap.get(input.getId());
        if (name != null) {
            input.setName(name);
        }
    }
}
