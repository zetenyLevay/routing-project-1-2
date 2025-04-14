package com.group.routingenginemain.gtfsparser;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.sql.*;

public class DataConverter {

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:gtfs.db")) {
            Statement stmt = conn.createStatement();

            convertCsvToTable(conn, "agency.txt", "agency",
                    "agency_id TEXT PRIMARY KEY, " +
                            "agency_name TEXT NOT NULL, " +
                            "agency_url TEXT NOT NULL, " +
                            "agency_timezone TEXT NOT NULL, " +
                            "agency_lang TEXT, " +
                            "agency_phone TEXT, " +
                            "agency_fare_url TEXT, " +
                            "agency_email TEXT");

            convertCsvToTable(conn, "calendar_dates.txt", "calendar_dates",
                    "service_id TEXT NOT NULL, " +
                            "date TEXT NOT NULL, " +
                            "exception_type INTEGER NOT NULL, " +
                            "PRIMARY KEY (service_id, date)");

            convertCsvToTable(conn, "feed_info.txt", "feed_info",
                    "feed_id TEXT, " +
                            "feed_publisher_name TEXT NOT NULL, " +
                            "feed_publisher_url TEXT NOT NULL, " +
                            "feed_lang TEXT NOT NULL, " +
                            "default_lang TEXT, " +
                            "feed_start_date TEXT, " +
                            "feed_end_date TEXT, " +
                            "feed_version TEXT, " +
                            "feed_contact_email TEXT, " +
                            "feed_contact_url TEXT");

            convertCsvToTable(conn, "pathways.txt", "pathways",
                    "pathway_id TEXT PRIMARY KEY, " +
                            "from_stop_id TEXT NOT NULL, " +
                            "to_stop_id TEXT NOT NULL, " +
                            "pathway_mode INTEGER NOT NULL, " +
                            "is_bidirectional INTEGER NOT NULL, " +
                            "length REAL, " +
                            "traversal_time INTEGER, " +
                            "stair_count INTEGER, " +
                            "max_slope REAL, " +
                            "min_width REAL, " +
                            "signposted_as TEXT, " +
                            "reversed_signposted_as TEXT, " +
                            "FOREIGN KEY(from_stop_id) REFERENCES stops(stop_id), " +
                            "FOREIGN KEY(to_stop_id) REFERENCES stops(stop_id)");

            convertCsvToTable(conn, "routes.txt", "routes",
                    "route_id TEXT PRIMARY KEY, " +
                            "agency_id TEXT REFERENCES agency(agency_id), " +
                            "route_short_name TEXT, " +
                            "route_long_name TEXT, " +
                            "route_desc TEXT, " +
                            "route_type INTEGER NOT NULL, " +
                            "route_url TEXT, " +
                            "route_color TEXT, " +
                            "route_text_color TEXT, " +
                            "route_sort_order INTEGER, " +
                            "continuous_pickup INTEGER, " +
                            "continuous_drop_off INTEGER, " +
                            "network_id TEXT");

            convertCsvToTable(conn, "shapes.txt", "shapes",
                    "shape_id TEXT NOT NULL, " +
                            "shape_pt_lat REAL NOT NULL, " +
                            "shape_pt_lon REAL NOT NULL, " +
                            "shape_pt_sequence INTEGER NOT NULL, " +
                            "shape_dist_traveled REAL, " +
                            "PRIMARY KEY(shape_id, shape_pt_sequence)");

            convertCsvToTable(conn, "stop_times.txt", "stop_times",
                    "trip_id TEXT NOT NULL, " +
                            "arrival_time TEXT, " +
                            "departure_time TEXT, " +
                            "stop_id TEXT, " +
                            "location_group_id TEXT, " +
                            "location_id TEXT, " +
                            "stop_sequence INTEGER NOT NULL, " +
                            "stop_headsign TEXT, " +
                            "start_pickup_drop_off_window TEXT, " +
                            "end_pickup_drop_off_window TEXT, " +
                            "pickup_type INTEGER, " +
                            "drop_off_type INTEGER, " +
                            "continuous_pickup INTEGER, " +
                            "continuous_drop_off INTEGER, " +
                            "shape_dist_traveled REAL, " +
                            "timepoint INTEGER, " +
                            "pickup_booking_rule_id TEXT, " +
                            "drop_off_booking_rule_id TEXT, " +
                            "PRIMARY KEY (trip_id, stop_sequence), " +
                            "FOREIGN KEY (trip_id) REFERENCES trips(trip_id), " +
                            "FOREIGN KEY (stop_id) REFERENCES stops(stop_id), " +
                            "FOREIGN KEY (location_group_id) REFERENCES location_groups(location_group_id), " +
                            "FOREIGN KEY (pickup_booking_rule_id) REFERENCES booking_rules(booking_rule_id), " +
                            "FOREIGN KEY (drop_off_booking_rule_id) REFERENCES booking_rules(booking_rule_id)");

            convertCsvToTable(conn, "stops.txt", "stops",
                    "stop_id TEXT PRIMARY KEY, " +
                            "stop_code TEXT, " +
                            "stop_name TEXT, " +
                            "tts_stop_name TEXT, " +
                            "stop_desc TEXT, " +
                            "stop_lat REAL, " +
                            "stop_lon REAL, " +
                            "zone_id TEXT, " +
                            "stop_url TEXT, " +
                            "location_type INTEGER, " +
                            "location_sub_type INTEGER, " +
                            "parent_station TEXT REFERENCES stops(stop_id), " +
                            "stop_timezone TEXT, " +
                            "wheelchair_boarding INTEGER, " +
                            "level_id TEXT, " +
                            "platform_code TEXT");

            convertCsvToTable(conn, "trips.txt", "trips",
                    "trip_id TEXT PRIMARY KEY, " +
                            "route_id TEXT NOT NULL, " +
                            "service_id TEXT NOT NULL, " +
                            "trip_headsign TEXT, " +
                            "trip_short_name TEXT, " +
                            "direction_id INTEGER, " +
                            "block_id TEXT, " +
                            "shape_id TEXT, " +
                            "wheelchair_accessible INTEGER, " +
                            "bikes_allowed INTEGER, " +
                            "FOREIGN KEY (route_id) REFERENCES routes(route_id), " +
                            "FOREIGN KEY (service_id) REFERENCES calendar(service_id), " +
                            "FOREIGN KEY (shape_id) REFERENCES shapes(shape_id)");

            createGTFSIndexes(conn);

            stmt.close();
        }
    }

    private static void convertCsvToTable(Connection conn, String csvFile,
                                          String tableName, String schema) throws Exception {
        System.out.println("Creating and loading table: " + tableName);
        conn.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" + schema + ")");

        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/gtfs/" + csvFile))) {
            String[] headers = reader.readNext();
            if (headers == null) return;

            String insertSQL = "INSERT INTO " + tableName +
                    " (" + String.join(",", headers) + ") VALUES (" +
                    String.join(",", java.util.Collections.nCopies(headers.length, "?")) + ")";


            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                String[] nextLine;
                int lineNum = 2;

                while ((nextLine = reader.readNext()) != null) {
                    try {
                        for (int i = 0; i < headers.length; i++) {
                            String value = nextLine[i];
                            pstmt.setString(i + 1, value.isEmpty() ? null : value);
                        }
                        pstmt.addBatch();
                        lineNum++;
                    } catch (Exception e) {
                        System.err.println("Error in " + csvFile + " at line " + lineNum + ": " + String.join(",", nextLine));
                        throw e;
                    }
                }
                pstmt.executeBatch();
            }
        }
    }


    public static void createGTFSIndexes(Connection conn) throws SQLException {
        System.out.println("Creating GTFS indexes...");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stops_parent ON stops(parent_station)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_routes_agency ON routes(agency_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_routes_type ON routes(route_type)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_trips_route ON trips(route_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_trips_service ON trips(service_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_trips_shape ON trips(shape_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_trips_block ON trips(block_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_trips_direction ON trips(direction_id)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stop_times_trip ON stop_times(trip_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stop_times_stop ON stop_times(stop_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stop_times_stop_arrival ON stop_times(stop_id, arrival_time)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stop_times_location_group ON stop_times(location_group_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_stop_times_location ON stop_times(location_id)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_calendar_dates_date ON calendar_dates(date)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_calendar_dates_service ON calendar_dates(service_id)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_shapes_shape ON shapes(shape_id)");


        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_pathways_from ON pathways(from_stop_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_pathways_to ON pathways(to_stop_id)");
        conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_pathways_from_to ON pathways(from_stop_id, to_stop_id)");

        System.out.println("GTFS indexes created successfully");
    }
}
