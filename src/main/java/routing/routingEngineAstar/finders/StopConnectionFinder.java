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
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.Stop.Stop;

/**
 * Finds valid connections from a given stop based on time constraints
 */
public class StopConnectionFinder {

    private final DBConnectionManager dbManager;
    private final TimeConstraintValidator timeValidator;

    public StopConnectionFinder(DBConnectionManager dbManager) {
        this.dbManager = dbManager;
        this.timeValidator = new TimeConstraintValidator();
    }

    /**
     * Finds all valid route steps from a given stop after a specific time
     */
    public List<RouteStep> findValidConnections(Stop fromStop, String currentTime) {
        List<RouteStep> validSteps = new ArrayList<>();

        String query = """
            SELECT DISTINCT 
                st1.stop_id as from_stop_id,
                st1.arrival_time as from_arrival,
                st1.departure_time as from_departure,
                st2.stop_id as to_stop_id,
                st2.arrival_time as to_arrival,
                st2.departure_time as to_departure,
                r.route_id,
                r.route_short_name,
                r.route_long_name,
                t.trip_id,
                t.trip_headsign
            FROM stop_times st1
            JOIN stop_times st2 ON st1.trip_id = st2.trip_id 
                AND st2.stop_sequence > st1.stop_sequence
            JOIN trips t ON st1.trip_id = t.trip_id
            JOIN routes r ON t.route_id = r.route_id
            JOIN stops s2 ON st2.stop_id = s2.stop_id
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
                    String toArrival = rs.getString("to_arrival");

                    // Validate time constraint
                    if (timeValidator.isValidTimeConnection(currentTime, fromDeparture)) {
                        RouteStep step = createRouteStep(rs);
                        if (step != null) {
                            validSteps.add(step);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding connections from stop " + fromStop.getStopID() + ": " + e.getMessage());
        }

        return validSteps;
    }

    /**
     * Creates a RouteStep from database result set
     */
    private RouteStep createRouteStep(ResultSet rs) throws SQLException {
        String toStopId = rs.getString("to_stop_id");
        String routeShortName = rs.getString("route_short_name");
        String departureTime = rs.getString("from_departure");
        String arrivalTime = rs.getString("to_arrival");

        // Get destination stop
        Stop toStop = getStopById(toStopId);
        if (toStop == null) {
            return null;
        }

        // Calculate duration in minutes
        double durationMinutes = calculateDurationMinutes(departureTime, arrivalTime);

        // Create mode of transport string
        String modeOfTransport = routeShortName != null ? routeShortName : "Transit";

        // Create stop description
        String stopStr = toStop.getStopName() + " (" + toStopId + ")";

        // Use the updated constructor
        return new RouteStep(modeOfTransport, toStop, durationMinutes, departureTime, arrivalTime, stopStr);
    }

    /**
     * Gets a stop by ID from the database
     */
    private Stop getStopById(String stopId) {
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_id = ?";

        try (Connection conn = dbManager.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, stopId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stopName = rs.getString("stop_name");
                    double lat = rs.getDouble("stop_lat");
                    double lon = rs.getDouble("stop_lon");
                    Coordinates coordinates = new Coordinates(lat, lon);
                    return new Stop(stopId, stopName, coordinates);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting stop by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Calculates duration in minutes between two times
     */
    private double calculateDurationMinutes(String startTime, String endTime) {
        try {
            int startSeconds = timeToSeconds(startTime);
            int endSeconds = timeToSeconds(endTime);

            // Handle day rollover
            if (endSeconds < startSeconds) {
                endSeconds += 24 * 3600;
            }

            return (endSeconds - startSeconds) / 60.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Converts time string to seconds since midnight
     */
    private int timeToSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }
}