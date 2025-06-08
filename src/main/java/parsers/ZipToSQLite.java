// ── File: parsers/ZipToSQLite.java ──────────────────────────────────────
package parsers;

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ZipToSQLite.java
 *
 * This class provides functionality to load GTFS data from a ZIP file into an
 * SQLite database, or to validate an existing SQLite .db file. The run(...)
 * method now returns the actual database filename that got loaded (e.g.
 * "budapest_gtfs.db"), so that the caller can construct a JDBC URL at runtime.
 *
 * Usage: String dbName = ZipToSQLite.run("path/to/some-gtfs.zip");
 */
public class ZipToSQLite {

    /**
     * Main method to run the ZipToSQLite functionality from the command line.
     * It accepts a single argument (either .zip or .db). On success, prints
     * {"ok":"loaded"}. On error, prints {"error":"…"} and exits.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "UTF-8");
        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            if (!scanner.hasNextLine()) {
                scanner.close();
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            JsonNode root;
            try {
                root = mapper.readTree(line);
            } catch (JsonProcessingException e) {
                System.out.println("{\"error\":\"Bad JSON input\"}");
                System.exit(1);
                return;
            } catch (IOException e) {
                System.out.println("{\"error\":\"Bad JSON input\"}");
                System.exit(1);
                return;
            }

            if (!root.isObject()) {
                System.out.println("{\"error\":\"Bad request\"}");
                continue;
            }

            JsonNode loadNode = root.get("load");
            if (loadNode == null || !loadNode.isTextual()) {
                System.out.println("{\"error\":\"Bad request\"}");
                continue;
            }

            String filename = loadNode.asText().trim();
            File f = new File(filename);
            if (!f.exists() || !f.isFile()) {
                System.out.println("{\"error\":\"File not found\"}");
                System.exit(1);
                return;
            }

            String lc = filename.toLowerCase();
            if (lc.endsWith(".db") || lc.endsWith(".zip")) {
                try {
                    // run(...) now returns the name of the .db that was loaded/validated
                    String actualDbName = run(filename);
                    System.out.println("{\"ok\":\"loaded\"}");
                    continue;
                } catch (IOException ioe) {
                    String msg = escapeForJson(ioe.getMessage());
                    System.out.println("{\"error\":\"" + msg + "\"}");
                    System.exit(1);
                    return;
                } catch (SQLException sqle) {
                    String msg = escapeForJson(sqle.getMessage());
                    System.out.println("{\"error\":\"" + msg + "\"}");
                    System.exit(1);
                    return;
                }
            }

            System.out.println("{\"error\":\"Bad request\"}");
        }
    }

    /**
     * Load a .zip or .db file. If .db, just validate connectivity; if .zip,
     * create/overwrite the corresponding .db file and populate its tables.
     * Returns the filename of the .db (for .zip, it's the computed name; for
     * .db input, it's the same as the input filename).
     *
     * @param fileName path to a .zip or .db
     * @return the actual .db filename on disk
     * @throws IOException if I/O fails or input is missing
     * @throws SQLException if any SQLite error occurs
     */
    public static String run(String fileName) throws IOException, SQLException {
        File f = new File(fileName);
        if (!f.exists() || !f.isFile()) {
            throw new IOException("File not found: " + fileName);
        }

        String lc = fileName.toLowerCase();
        if (lc.endsWith(".db")) {
            // Just validate opening the database; no further action
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + fileName)) {
                // if this succeeds, the DB is valid
            }
            return fileName;
        }

        if (lc.endsWith(".zip")) {
            String dbName = computeDbName(f.getName());
            boolean dbAlreadyExisted = new File(dbName).exists();

            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName)) {
                configureDatabase(conn);
                processZipFile(fileName, conn, dbAlreadyExisted);
            }
            return dbName;
        }

        throw new IllegalArgumentException("Unsupported file type: " + fileName);
    }

    /**
     * Safely escape any backslashes or quotes inside an exception message so
     * that we can embed it in JSON.
     */
    private static String escapeForJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Given "foo.zip" (or "foo.BERLIN.ZIP"), returns "foo.db" (dropping
     * everything after the last dot). If there is no dot at all, just returns
     * zipName + ".db".
     */
    private static String computeDbName(String zipName) {
        if (!zipName.contains(".")) {
            return zipName + ".db";
        }
        return zipName.substring(0, zipName.lastIndexOf('.')) + ".db";
    }

    /**
     * Set SQLite PRAGMA options for better performance.
     */
    private static void configureDatabase(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA journal_mode = WAL;");
        }
    }

    /**
     * If the .db already existed on disk, do nothing. Otherwise, open the ZIP
     * and process every CSV/TXT entry.
     */
    private static void processZipFile(String zipPath, Connection conn, boolean dbExisted)
            throws IOException, SQLException {
        if (dbExisted) {
            return;
        }
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            processZipEntries(zipFile, conn);
        }
    }

    private static void processZipEntries(ZipFile zipFile, Connection conn)
            throws IOException, SQLException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String entryNameLower = entry.getName().toLowerCase();
            if ((entryNameLower.endsWith(".txt") || entryNameLower.endsWith(".csv"))
                    && isCSVformat(zipFile, entry)) {
                processCsvEntry(zipFile, entry, entry.getName(), conn);
            }
            // Otherwise, skip silently
        }
    }

    private static void processCsvEntry(ZipFile zipFile, ZipEntry entry, String entryName, Connection conn)
            throws IOException, SQLException {
        String tableName = sanitizeTableName(entryName);

        try (InputStream is = zipFile.getInputStream(entry); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8); CSVParser parser = CSVParser.parse(r, CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreEmptyLines(true)
                .withTrim())) {

            // ←— **NEW**: Strip any leading BOMs or stray spaces before sanitizing
            List<String> rawHeaders = parser.getHeaderNames();
            List<String> headers = rawHeaders.stream()
                    .map(h -> h.replaceAll("^[\\uFEFF\\s]+", "")) // drop BOMs or leading whitespace
                    .map(h -> h.trim()) // trim any other stray spaces
                    .collect(Collectors.toList());

            // …then continue exactly as before:
            createTable(conn, tableName, headers);
            insertCsvData(conn, tableName, headers, parser);

        }
    }

    public static void createIndexes(String entryName, Connection conn) throws SQLException {
         String tableName = sanitizeTableName(entryName);

        if ("stop_times".equals(tableName)) {
            try (Statement st = conn.createStatement()) {
                // lookup by stop_id
                st.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_stop_times_stop_id "
                        + "ON stop_times (stop_id);"
                );
                st.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_stop_times_trip_seq "
                        + "ON stop_times (trip_id, stop_sequence);"
                );
            }
        }
    }

    private static String sanitizeTableName(String entryName) {
        String fileName = entryName.contains("/")
                ? entryName.substring(entryName.lastIndexOf('/') + 1)
                : entryName;
        String base = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;
        return base.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * Create a new table with the given name and headers. If the table already
     * exists, it will be dropped first. All columns are created as TEXT.
     */
    private static void createTable(Connection conn, String tableName, List<String> headers)
            throws SQLException {
        StringBuilder ddl = new StringBuilder();
        ddl.append("DROP TABLE IF EXISTS ").append(tableName).append(";");
        ddl.append("CREATE TABLE ").append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                ddl.append(", ");
            }
            String col = headers.get(i).replaceAll("[^A-Za-z0-9_]", "_");
            ddl.append(col).append(" TEXT");
        }
        ddl.append(");");

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(ddl.toString());
        }
    }

    /**
     * Inserts the CSV data into the specified table. It uses a prepared
     * statement to batch insert rows for better performance.
     */
    private static int insertCsvData(Connection conn, String tableName, List<String> headers, CSVParser parser)
            throws SQLException {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("INSERT INTO ").append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) {
                insertSql.append(", ");
            }
            insertSql.append(headers.get(i).replaceAll("[^A-Za-z0-9_]", "_"));
        }
        insertSql.append(") VALUES (")
                .append(String.join(",", Collections.nCopies(headers.size(), "?")))
                .append(");");

        conn.setAutoCommit(false);
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertSql.toString())) {
            for (CSVRecord rec : parser) {
                if (rec.size() != headers.size()) {
                    // Skip rows with the wrong number of fields
                    continue;
                }
                for (int i = 0; i < headers.size(); i++) {
                    String val = rec.get(i);
                    ps.setString(i + 1, (val == null ? "" : val.trim()));
                }
                ps.addBatch();
                count++;
                if (count % 1000 == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
        }
        return count;
    }

    /**
     * Heuristic: read the first line of the entry. If it contains commas or any
     * of the words "agency", "route", "stop", "trip" (case‐insensitive), and
     * there is at least one more non‐empty line, treat it as a CSV. Otherwise,
     * return false.
     */
    private static boolean isCSVformat(ZipFile zipFile, ZipEntry entry) {
        try (InputStream is = zipFile.getInputStream(entry); Reader r = new InputStreamReader(is, "UTF-8"); java.io.BufferedReader br = new java.io.BufferedReader(r)) {

            String firstLine = br.readLine();
            if (firstLine == null) {
                return false;
            }
            String lower = firstLine.toLowerCase();
            if (firstLine.contains(",")
                    || lower.contains("agency")
                    || lower.contains("route")
                    || lower.contains("stop")
                    || lower.contains("trip")) {
                int lines = 1;
                while (br.readLine() != null && lines < 2) {
                    lines++;
                }
                return lines >= 2;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
