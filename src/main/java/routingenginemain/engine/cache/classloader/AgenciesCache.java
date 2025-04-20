package routingenginemain.engine.cache.classloader;

import routingenginemain.model.Agency;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AgenciesCache {
    private static final Map<String, Agency> AGENCIES = new HashMap<>();

    public static void init() {

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:gtfs.db");
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM agency");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("agency_id");
                String name = rs.getString("agency_name");
                AGENCIES.put(id, new Agency(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Agency getAgency(String id) {
        return AGENCIES.get(id);
    }
}
