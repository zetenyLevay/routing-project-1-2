package routing.routingEngineAstar.finders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.validators.TimeConstraintValidator;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteInfo;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;

/**
 * Finds valid connections from a given stop based on time constraints.
 */
public class StopConnectionFinder {

    private final DBConnectionManager dbManager;
    private final TimeConstraintValidator timeValidator;

    public StopConnectionFinder(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.timeValidator = new TimeConstraintValidator();
    }

    /**
     * Finds all valid route steps from a given stop after a specific time.
     */
    public List<RouteStep> findValidConnections(Stop fromStop, String currentTime) {
        List<RouteStep> validSteps = new ArrayList<>();

        // Note: We now JOIN routes -> agency so that we can pull agency_name as operator_name.
        String query = """
            SELECT DISTINCT
                st1.stop_id        AS from_stop_id,
                st1.arrival_time   AS from_arrival,
                st1.departure_time AS from_departure,
                st2.stop_id        AS to_stop_id,
                st2.arrival_time   AS to_arrival,
                st2.departure_time AS to_departure,
                r.route_id,
                r.route_short_name AS route_short_name,
                r.route_long_name  AS route_long_name,
                t.trip_id,
                t.trip_headsign    AS trip_headsign,
                a.agency_name      AS operator_name
            FROM stop_times st1
            JOIN stop_times st2
              ON st1.trip_id = st2.trip_id
             AND st2.stop_sequence > st1.stop_sequence
            JOIN trips t
              ON st1.trip_id = t.trip_id
            JOIN routes r
              ON t.route_id = r.route_id
            JOIN agency a
              ON r.agency_id = a.agency_id
            JOIN stops s2
              ON st2.stop_id = s2.stop_id
            WHERE st1.stop_id = ?
              AND st1.departure_time >= ?
            ORDER BY st1.departure_time, st2.arrival_time
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fromStop.getStopID());
            stmt.setString(2, currentTime);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String fromDeparture = rs.getString("from_departure");
                    String toArrival     = rs.getString("to_arrival");

                    // Validate the time constraint (e.g. transfer windows, etc.)
                    if (timeValidator.isValidTimeConnection(currentTime, fromDeparture)) {
                        RouteStep step = createRouteStep(rs);
                        if (step != null) {
                            validSteps.add(step);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding connections from stop " +
                               fromStop.getStopID() + ": " + e.getMessage());
        }

        return validSteps;
    }

    /**
     * Builds a RouteStep object from the current row in ResultSet.
     * Now correctly extracts operator_name, route_short_name, route_long_name, trip_headsign.
     */
    private RouteStep createRouteStep(ResultSet rs) throws SQLException {
        // 1) Fetch the “to” stop’s ID & look it up in the stops table:
        String toStopId = rs.getString("to_stop_id");
        Stop toStop = getStopById(toStopId);
        if (toStop == null) {
            // If we couldn’t find the Stop in stops table, skip this row.
            return null;
        }

        // 2) Calculate how many minutes this trip leg takes:
        String departureTime = rs.getString("from_departure");
        String arrivalTime   = rs.getString("to_arrival");
        double durationMinutes = calculateDurationMinutes(departureTime, arrivalTime);

        // 3) Mode-of-transport string (fall back to “Transit” if short name is null)
        String routeShortName = rs.getString("route_short_name");
        String modeOfTransport = (routeShortName != null ? routeShortName : "Transit");

        // 4) A human‐readable “stop description” just for logging/UIs:
        String stopStr = toStop.getStopName() + " (" + toStopId + ")";

        // 5) Pull operator, short name, long name, headsign from the result‐set:
        String operatorName = rs.getString("operator_name");         // agency.agency_name
        String shortName    = rs.getString("route_short_name");      // routes.route_short_name
        String longName     = rs.getString("route_long_name");       // routes.route_long_name
        String headSign     = rs.getString("trip_headsign");         // trips.trip_headsign

        // 6) Build a proper RouteInfo using exactly (operator, shortName, longName, headSign)
        RouteInfo routeInfo = new RouteInfo(
            operatorName,
            shortName,
            longName,
            headSign
        );

        // 7) Finally, assemble and return the RouteStep:
        return new RouteStep(
            modeOfTransport,
            toStop,
            durationMinutes,
            departureTime,
            arrivalTime,
            stopStr,
            routeInfo
        );
    }

    /**
     * Utility: Lookup a Stop by its ID (from the 'stops' table).
     */
    private Stop getStopById(String stopId) {
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon "
                     + "FROM stops WHERE stop_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, stopId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stopName = rs.getString("stop_name");
                    double lat = rs.getDouble("stop_lat");
                    double lon = rs.getDouble("stop_lon");
                    Coordinates coords = new Coordinates(lat, lon);
                    return new Stop(stopId, stopName, coords);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting stop by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Calculates duration (in minutes) between two “HH:mm:ss” strings,
     * accounting for possible day rollover.
     */
    private double calculateDurationMinutes(String startTime, String endTime) {
        try {
            int startSeconds = timeToSeconds(startTime);
            int endSeconds   = timeToSeconds(endTime);

            // If the end‐time rolled over past midnight:
            if (endSeconds < startSeconds) {
                endSeconds += 24 * 3600;
            }
            return (endSeconds - startSeconds) / 60.0;
        } catch (Exception e) {
            // If parsing fails for some reason, default to 0.
            return 0.0;
        }
    }

    /**
     * Helper: “HH:mm:ss” → seconds since midnight.
     */
    private int timeToSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
        int hours   = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
}
