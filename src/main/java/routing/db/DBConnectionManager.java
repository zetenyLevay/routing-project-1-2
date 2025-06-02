package routing.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnectionManager is responsible for creating (and potentially caching)
 * a JDBC Connection to a single SQLite database file (your GTFS‐loaded .db).
 *
 * Usage:
 *   DBConnectionManager mgr = new DBConnectionManager("jdbc:sqlite:budapest_gtfs.db");
 *   try (Connection conn = mgr.getConnection()) {
 *     // do some queries
 *   }
 */
public class DBConnectionManager {
    private final String jdbcUrl;

    /**
     * Construct a DBConnectionManager for a given JDBC URL.
     * For SQLite, that URL will usually look like: "jdbc:sqlite:/path/to/my_gtfs.db"
     */
    public DBConnectionManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Returns a new Connection to the underlying database.
     * Caller is responsible for closing this Connection (e.g. via try‐with‐resources).
     *
     * If you want to cache connections or use a connection pool, you can modify this
     * method to do so. For simplicity, it just calls DriverManager.getConnection(...) 
     * each time.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
