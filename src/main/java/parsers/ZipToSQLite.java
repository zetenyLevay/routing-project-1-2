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
        run("data/budapest_gtfs.zip");
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
            // Uncomment if you want interactive queries
            // handleUserQueries(conn);
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
            
            // Skip directories
            if (entry.isDirectory()) {
                continue;
            }
            
            // Process CSV and TXT files (including agency.txt)
            if ((entryName.endsWith(".txt") || entryName.endsWith(".csv")) && isCSVformat(zipFile, entry)) {
                processCsvEntry(zipFile, entry, entryName, conn);
            } else if (entryName.endsWith(".txt") || entryName.endsWith(".csv")) {
                System.out.println("Skipping " + entryName + " - not detected as CSV format");
            }
        }
    }

    private static void processCsvEntry(ZipFile zipFile, ZipEntry entry, String entryName, Connection conn) {
        String tableName = sanitizeTableName(entryName);
        System.out.println("Loading " + entryName + " into table " + tableName);

        try (InputStream inputStream = zipFile.getInputStream(entry);
             Reader reader = new InputStreamReader(inputStream, "UTF-8");
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreEmptyLines(true).withTrim())) {
            
            List<String> headers = parser.getHeaderNames();
            if (headers.isEmpty()) {
                System.out.println("Warning: No headers found in " + entryName);
                return;
            }
            
            System.out.println("Headers found: " + headers);
            createTable(conn, tableName, headers);
            int rowCount = insertCsvData(conn, tableName, headers, parser);
            System.out.println("Completed loading " + entryName + " - " + rowCount + " rows inserted");
            
        } catch (IOException e) {
            System.out.println("IO error in " + entryName + ": " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("DB error in " + entryName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTable(Connection conn, String tableName, List<String> headers) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Sanitize column names to avoid SQL issues
            StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
            createTableSQL.append(tableName).append(" (");
            
            for (int i = 0; i < headers.size(); i++) {
                if (i > 0) createTableSQL.append(", ");
                String columnName = headers.get(i).replaceAll("[^A-Za-z0-9_]", "_");
                createTableSQL.append(columnName).append(" TEXT");
            }
            createTableSQL.append(")");
            
            // Drop existing table first
            stmt.execute("DROP TABLE IF EXISTS " + tableName);
            stmt.execute(createTableSQL.toString());
            System.out.println("Created table: " + tableName);
        }
    }

    private static int insertCsvData(Connection conn, String tableName, List<String> headers, CSVParser parser) throws SQLException {
        // Sanitize column names for the INSERT statement
        StringBuilder insertSQL = new StringBuilder("INSERT INTO ");
        insertSQL.append(tableName).append(" (");
        
        for (int i = 0; i < headers.size(); i++) {
            if (i > 0) insertSQL.append(", ");
            String columnName = headers.get(i).replaceAll("[^A-Za-z0-9_]", "_");
            insertSQL.append(columnName);
        }
        insertSQL.append(") VALUES (");
        insertSQL.append(String.join(",", Collections.nCopies(headers.size(), "?")));
        insertSQL.append(")");
        
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString())) {
            conn.setAutoCommit(false);
            int batchSize = 1000;
            int count = 0;
            
            for (CSVRecord record : parser) {
                // Skip records that don't have the expected number of columns
                if (record.size() != headers.size()) {
                    System.out.println("Warning: Record " + (count + 1) + " has " + record.size() + 
                                     " columns, expected " + headers.size() + ". Skipping.");
                    continue;
                }
                
                for (int i = 0; i < headers.size(); i++) {
                    String value = record.get(i);
                    pstmt.setString(i + 1, value != null ? value.trim() : "");
                }
                pstmt.addBatch();
                count++;
                
                if (count % batchSize == 0) {
                    pstmt.executeBatch();
                    conn.commit();
                    System.out.println("Processed " + count + " rows...");
                }
            }
            
            // Execute remaining batch
            if (count % batchSize != 0) {
                pstmt.executeBatch();
                conn.commit();
            }
            
            return count;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static void listTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")) {
            System.out.println("\n=== Tables in Database ===");
            while (rs.next()) {
                String tableName = rs.getString("name");
                System.out.println("- " + tableName);
                
                // Show row count for each table
                try (Statement countStmt = conn.createStatement();
                     ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                    if (countRs.next()) {
                        System.out.println("  (" + countRs.getInt(1) + " rows)");
                    }
                }
            }
            System.out.println("==========================\n");
        }
    }

    private static void handleUserQueries(Connection conn) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Database loading complete. You can now run SQL queries.");
        System.out.println("Type 'exit' to quit, 'tables' to list tables, or enter a SQL query:");
        
        while (true) {
            System.out.print("\nSQL> ");
            String query = scanner.nextLine().trim();
            
            if (query.equalsIgnoreCase("exit")) {
                break;
            } else if (query.equalsIgnoreCase("tables")) {
                try {
                    listTables(conn);
                } catch (SQLException e) {
                    System.out.println("Error listing tables: " + e.getMessage());
                }
            } else if (!query.isEmpty()) {
                executeUserQuery(conn, query);
            }
        }
        scanner.close();
    }

    private static void executeUserQuery(Connection conn, String query) {
        try (Statement queryStmt = conn.createStatement()) {
            boolean hasResult = queryStmt.execute(query);
            if (hasResult) {
                try (ResultSet rs = queryStmt.getResultSet()) {
                    printResultSet(rs);
                }
            } else {
                int updateCount = queryStmt.getUpdateCount();
                System.out.println("Query executed. Rows affected: " + updateCount);
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        
        // Print headers
        for (int i = 1; i <= columnCount; i++) {
            System.out.print(meta.getColumnName(i));
            if (i < columnCount) System.out.print("\t");
        }
        System.out.println();
        
        // Print separator
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("--------");
            if (i < columnCount) System.out.print("\t");
        }
        System.out.println();
        
        // Print data
        int rowCount = 0;
        while (rs.next() && rowCount < 100) { // Limit to 100 rows for readability
            for (int i = 1; i <= columnCount; i++) {
                String value = rs.getString(i);
                System.out.print(value != null ? value : "NULL");
                if (i < columnCount) System.out.print("\t");
            }
            System.out.println();
            rowCount++;
        }
        
        if (rowCount == 100) {
            System.out.println("... (showing first 100 rows only)");
        }
    }

    /**
     * Enhanced CSV format detection - specifically improved for GTFS files
     */
    private static boolean isCSVformat(ZipFile zipFile, ZipEntry entry) {
        try (InputStream inputStream = zipFile.getInputStream(entry);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                return false;
            }
            
            // Check if first line looks like CSV headers (contains commas or typical GTFS headers)
            if (firstLine.contains(",") || 
                firstLine.toLowerCase().contains("agency") || 
                firstLine.toLowerCase().contains("route") || 
                firstLine.toLowerCase().contains("stop") ||
                firstLine.toLowerCase().contains("trip")) {
                
                // Count total lines to ensure it's not just a header
                int lineCount = 1;
                while (reader.readLine() != null && lineCount < 50) {
                    lineCount++;
                }
                
                // File should have at least a header + 1 data row
                return lineCount >= 2;
            }
            
            return false;
        } catch (IOException e) {
            System.out.println("Error checking CSV format for " + entry.getName() + ": " + e.getMessage());
            return false;
        }
    }
}