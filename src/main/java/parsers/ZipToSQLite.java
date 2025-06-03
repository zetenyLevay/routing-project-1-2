package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ZipToSQLite {



    public static void main(String[] args) {

        run("data/june2ndBudapestGTFS.zip");
    }

    private static String DBName(String zipName) {
        return zipName.substring(0, zipName.lastIndexOf('.')) + ".db";
    }

    /**
     * Turn an entry name like "folder/sub/file-name.txt" into a safe table name "file_name"
     */
    private static String sanitizeTableName(String entryName) {
        // strip any path
        String fileName = entryName.contains("/")
                ? entryName.substring(entryName.lastIndexOf('/') + 1)
                : entryName;
        // remove extension
        String base = fileName.contains(".")
                ? fileName.substring(0, fileName.lastIndexOf('.'))
                : fileName;
        // replace any non-alphanumeric/underscore with underscore
        return base.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static void configureDatabase(Connection conn) throws SQLException {
        try (Statement statem = conn.createStatement()) {
            statem.execute("PRAGMA synchronous = NORMAL;");
            statem.execute("PRAGMA journal_mode = WAL;");
        }
    }

    public static void run(String fileName) {
        File selectedFile = new File(fileName);
        if (selectedFile == null) {
            System.out.println("No file selected, exiting");
            return;
        }

        String zipPath = selectedFile.getAbsolutePath();
        String dbName = DBName(selectedFile.getName());
        String dbPath = dbName;
        System.out.println("dbName " + dbName + " saved to: " + System.getProperty("user.dir"));

        File dbFile = new File(dbName);
        boolean dbExisted = dbFile.exists();

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            configureDatabase(conn);
            processZipFile(zipPath, conn, dbExisted);
            listTables(conn);
//            handleUserQueries(conn);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processZipFile(String zipPath, Connection conn, boolean dbExisted) {
        try (ZipFile zipFile = new ZipFile(zipPath)) {
            if (!dbExisted) {
                processZipEntries(zipFile, conn);
            } else {
                System.out.println("DB already exists, using existing one: " + new File(zipPath).getName());
            }
        } catch (IOException e) {
            System.out.println("!!Error with zip: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processZipEntries(ZipFile zipFile, Connection conn) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if ((entryName.endsWith(".txt") || entryName.endsWith(".csv")) && isCSVformat(zipFile, entry)) {
                processCsvEntry(zipFile, entry, entryName, conn);
            }
        }
    }

    private static void processCsvEntry(ZipFile zipFile, ZipEntry entry, String entryName, Connection conn) {

        String tableName = sanitizeTableName(entryName);
        System.out.println("loading " + entryName + " into table " + tableName);

        try (InputStream inputStream = zipFile.getInputStream(entry);
             Reader reader = new InputStreamReader(inputStream);
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            List<String> headers = parser.getHeaderNames();
            createTable(conn, tableName, headers);
            insertCsvData(conn, tableName, headers, parser);
        } catch (IOException e) {
            System.out.println("error in " + entryName + " error= " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("db error in: " + entryName + " error= " + e.getMessage());
        }
    }

    private static void createTable(Connection conn, String tableName, List<String> headers) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            String createTableSQL = "CREATE TABLE " + tableName + " (" +
                    String.join(" TEXT, ", headers) + " TEXT)";
            stmt.execute(createTableSQL);
        }
    }

    private static void insertCsvData(Connection conn, String tableName, List<String> headers, CSVParser parser) throws SQLException {
        String insertSQL = "INSERT INTO " + tableName + " (" +
                String.join(", ", headers) + ") VALUES (" +
                String.join(",", Collections.nCopies(headers.size(), "?")) + ")";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            conn.setAutoCommit(false);
            int batchSize = 1000;
            int count = 0;
            for (CSVRecord record : parser) {
                for (int i = 0; i < headers.size(); i++) {
                    pstmt.setString(i + 1, record.get(i));
                }
                pstmt.addBatch();
                count++;
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    System.out.println("current== " + count + " rows");
                }
            }
            if (count % batchSize != 0) {
                pstmt.executeBatch();
                conn.commit();
            }
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void listTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
            System.out.println("Tables in DB:");
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
            System.out.println();
        }
    }

//    private static void handleUserQueries(Connection conn) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Loading database done.");
//        while (true) {
//            System.out.print("Enter SQL or exit to quit: ");
//            String query = scanner.nextLine().trim();
//            if (query.equalsIgnoreCase("exit")) {
//                break;
//            }
//            executeUserQuery(conn, query);
//        }
//    }

    private static void executeUserQuery(Connection conn, String query) {
        try (Statement queryStmt = conn.createStatement()) {
            boolean hasResult = queryStmt.execute(query);
            if (hasResult) {
                try (ResultSet rs = queryStmt.getResultSet()) {
                    printResultSet(rs);
                }
            } else {
                System.out.println("No result set (update executed or no rows found)");
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(meta.getColumnName(i) + "\t");
        }
        System.out.println();
        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }
    }

    private static boolean isCSVformat(ZipFile zipFile, ZipEntry entry) {
        try (InputStream inputStream = zipFile.getInputStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            int count = 0;
            while (reader.readLine() != null) {
                count++;
                if (count > 11) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}