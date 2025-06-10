package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Parses a ZIP file containing GTFS data and converts it into an SQLite database.
 * If the input is already an SQLite database, it simply returns the file name.
 * 
 * The ZIP file should contain CSV files with GTFS data, which will be processed
 * and stored in the SQLite database.
 */
public class ZipToSQLite {


    @SuppressWarnings("unused")
    public static String run(String fileName) throws IOException, SQLException {
        File f = new File(fileName);
        String lc = fileName.toLowerCase();

        if (lc.endsWith(".db")) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + fileName)) { }
            return fileName;
        }

        if (!lc.endsWith(".zip")) {
            throw new IOException("Unsupported file type: " + fileName);
        }

        String dbName = computeDbName(f.getName());
        boolean existed = new File(dbName).exists();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName)) {
            configureDatabase(conn);

            if (!existed) {
                try (ZipFile zipFile = new ZipFile(fileName)) {
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.isDirectory()) continue;

                        String lower = entry.getName().toLowerCase();
                        if ((lower.endsWith(".txt") || lower.endsWith(".csv"))
                                && isCSVformat(zipFile, entry)) {
                            processCsvEntry(zipFile, entry, conn);
                        }
                    }
                }
            }
        }

        return dbName;
    }

    /**
     * Configures the SQLite database with specific settings.
     * 
     * @param conn The connection to the SQLite database.
     * @throws SQLException If there is an error configuring the database.
     */
    private static void configureDatabase(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA journal_mode = WAL;");
        }
    }

    /**
     * Processes a CSV entry from the ZIP file and inserts its data into the SQLite database.
     * 
     * @param zipFile The ZIP file containing the CSV entry.
     * @param entry The ZipEntry representing the CSV file.
     * @param conn The connection to the SQLite database.
     * @throws IOException If there is an error reading the CSV file.
     * @throws SQLException If there is an error inserting data into the database.
     */
    private static void processCsvEntry(ZipFile zipFile, ZipEntry entry,
                                        Connection conn)
            throws IOException, SQLException {

        String entryName = entry.getName();
        String tableName = sanitizeTableName(entryName);

        try (
            InputStream is = zipFile.getInputStream(entry);
            Reader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(ir)
        ) {
            String headerLine;
            do {
                headerLine = br.readLine();
            } while (headerLine != null && headerLine.trim().isEmpty());

            if (headerLine == null) {
                throw new IOException("Empty CSV: " + entryName);
            }
            headerLine = headerLine.replaceAll("^[\\uFEFF\\s]+", "");
            String[] rawH = splitCsvLine(headerLine);
            List<String> headers = new ArrayList<>();
            for (String h : rawH) headers.add(h.trim());

            createTable(conn, tableName, headers);

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append(" (");
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(headers.get(i).replaceAll("[^A-Za-z0-9_]", "_"));
            }
            sql.append(") VALUES (")
               .append(String.join(",", Collections.nCopies(headers.size(), "?")))
               .append(");");

            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                String line;
                int batch = 0;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] fields = splitCsvLine(line);
                    if (fields.length != headers.size()) continue;
                    for (int i = 0; i < fields.length; i++) {
                        ps.setString(i + 1, fields[i].trim());
                    }
                    ps.addBatch();
                    if (++batch % 1000 == 0) {
                        ps.executeBatch();
                        conn.commit();
                    }
                }
                ps.executeBatch();
                conn.commit();
            } finally {
                conn.setAutoCommit(true);
            }

            createIndexes(entryName, conn);
        }
    }

    private static String[] splitCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                parts.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        parts.add(sb.toString());
        return parts.toArray(new String[0]);
    }

    private static boolean isCSVformat(ZipFile zipFile, ZipEntry entry) {
        try (
            InputStream is = zipFile.getInputStream(entry);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
        ) {
            String first = br.readLine();
            if (first == null) return false;
            String lower = first.toLowerCase();

            if (first.contains(",")
             || lower.contains("agency")
             || lower.contains("route")
             || lower.contains("stop")
             || lower.contains("trip")) {

                int lines = 1;
                while (lines < 2 && br.readLine() != null) lines++;
                return lines >= 2;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates indexes for the specified table in the SQLite database.
     * 
     * @param entryName The name of the entry (table) to create indexes for.
     * @param conn The connection to the SQLite database.
     * @throws SQLException If there is an error creating the indexes.
     */
    public static void createIndexes(String entryName, Connection conn) throws SQLException {
        String tableName = sanitizeTableName(entryName);
        if ("stop_times".equals(tableName)) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_stop_times_stop_id " +
                    "ON stop_times (stop_id);"
                );
                st.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_stop_times_trip_seq " +
                    "ON stop_times (trip_id, stop_sequence);"
                );
                st.execute("CREATE INDEX IF NOT EXISTS idx_stop_times_trip_id ON stop_times (trip_id);");
                st.execute("CREATE INDEX IF NOT EXISTS idx_trips_route_id ON trips (route_id);");
                st.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_stop_times_unique ON stop_times (trip_id, stop_sequence);");

            }
        }
    }


    /**
     * Creates a table in the SQLite database based on the provided headers.
     * 
     * @param conn The connection to the SQLite database.
     * @param tableName The name of the table to create.
     * @param headers The list of headers (column names) for the table.
     * @throws SQLException If there is an error creating the table.
     */
    private static void createTable(Connection conn, String tableName, List<String> headers)
            throws SQLException {
        StringBuilder ddl = new StringBuilder();
        ddl.append("DROP TABLE IF EXISTS ").append(tableName).append(";");
        ddl.append("CREATE TABLE ").append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) ddl.append(", ");
            String col = headers.get(i).replaceAll("[^A-Za-z0-9_]", "_");
            ddl.append(col).append(" TEXT");
        }
        ddl.append(");");
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl.toString());
        }
    }

    private static String sanitizeTableName(String entryName) {
        String file = entryName.contains("/") ?
                      entryName.substring(entryName.lastIndexOf('/') + 1) :
                      entryName;
        String base = file.contains(".") ?
                      file.substring(0, file.lastIndexOf('.')) :
                      file;
        return base.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String computeDbName(String zipName) {
        if (!zipName.contains(".")) return zipName + ".db";
        return zipName.substring(0, zipName.lastIndexOf('.')) + ".db";
    }

    @SuppressWarnings("unused")
    private static String escapeForJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
