package parsers;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.leastfixedpoint.json.JSONSyntaxError;

import routing.db.DBConnectionManager;
import routing.routingEngineAstar.RoutingEngineAstar;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteStep;

/**
 * RequestHandler.java
 *
 * This class reads JSON‐Line commands from stdin and writes JSON responses to stdout.
 * After a successful "load" command, it stores the new JDBC URL in `currentJdbcUrl`.
 * All subsequent "routeFrom"/"to"/"startingAt" commands use that URL.
 */
public class RequestHandler {

    private final CLIRead cliRead;
    private final CLIWrite cliWrite;

    private String currentJdbcUrl = null;

    public RequestHandler() {
        this.cliRead = new CLIRead();
        this.cliWrite = new CLIWrite();
    }

    public void run() throws IOException {
        while (true) {
            Object json;
            try {
                json = cliRead.read();
            } catch (JSONSyntaxError e) {
                cliWrite.sendError("Bad JSON input");
                break;
            } catch (EOFException e) {
                System.err.println("End of input detected");
                break;
            }

            if (!(json instanceof Map<?, ?>)) {
                cliWrite.sendError("Bad request");
                continue;
            }

            Map<?, ?> request = (Map<?, ?>) json;

            // --- ping / pong ---
            if (request.containsKey("ping")) {
                cliWrite.sendOk(Map.of("pong", request.get("ping")));
                continue;
            }

            // --- load filename ---
            if (request.containsKey("load")) {
                Object loadObj = request.get("load");
                if (!(loadObj instanceof String)) {
                    cliWrite.sendError("Bad request");
                    continue;
                }

                String selectedFile = (String) loadObj;
                try {
                    // ZipToSQLite.run(...) now returns the .db filename it created/validated
                    String dbFilename = ZipToSQLite.run(selectedFile);
                    // Build the full JDBC URL and store it
                    this.currentJdbcUrl = "jdbc:sqlite:" + dbFilename;
                    cliWrite.sendOk("loaded");
                } catch (Exception e) {
                    cliWrite.sendError(e.getMessage());
                    break;
                }
                continue;
            }

            if (request.containsKey("routeFrom")) {
                if (this.currentJdbcUrl == null) {
                    cliWrite.sendError("No database loaded");
                    continue;
                }

                // 1) Check that routeFrom is a Map<?,?>
                Object routeFromObj = request.get("routeFrom");
                if (!(routeFromObj instanceof Map<?, ?>)) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                Map<?, ?> routeFromMap = (Map<?, ?>) routeFromObj;

                // 2) Extract lat / lon from routeFromMap
                Double fromLat, fromLon;
                try {
                    fromLat = extractDouble(routeFromMap.get("lat"));
                    fromLon = extractDouble(routeFromMap.get("lon"));
                } catch (Exception e) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                Coordinates startPoint = new Coordinates(fromLat, fromLon);

                // 3) Check that 'to' is a Map<?,?>
                Object toObj = request.get("to");
                if (!(toObj instanceof Map<?, ?>)) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                Map<?, ?> toMap = (Map<?, ?>) toObj;

                // 4) Extract lat / lon from toMap
                Double toLat, toLon;
                try {
                    toLat = extractDouble(toMap.get("lat"));
                    toLon = extractDouble(toMap.get("lon"));
                } catch (Exception e) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                Coordinates endPoint = new Coordinates(toLat, toLon);

                // 5) Extract startingAt (must be a String "HH:mm" or "HH:mm:ss")
                Object startAtObj = request.get("startingAt");
                if (!(startAtObj instanceof String)) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                String startingAtStr = (String) startAtObj;
                // If it’s in "HH:mm" form, append ":00" so that findRoute can accept "HH:mm:ss"
                if (startingAtStr.matches("^\\d{1,2}:\\d{2}$")) {
                    startingAtStr = startingAtStr + ":00";
                }

                try {
                    // Instantiate a new RoutingEngineAstar using the current JDBC URL
                    DBConnectionManager connMgr = new DBConnectionManager(this.currentJdbcUrl);
                    RoutingEngineAstar router = new RoutingEngineAstar(connMgr);

                    List<RouteStep> route = router.findRoute(
                        startPoint.getLatitude(),
                        startPoint.getLongitude(),
                        endPoint.getLatitude(),
                        endPoint.getLongitude(),
                        startingAtStr
                    );
                    cliWrite.writeRouteSteps(route);
                } catch (Exception e) {
                    cliWrite.sendError(e.getMessage());
                }

                continue;
            }

            cliWrite.sendError("Bad request");
        }
    }

    /**
     * Extracts a double value from an Object, which should be a Number.
     * Throws IllegalArgumentException if the object is not a Number.
     *
     * @param o the Object to extract the double from
     * @return the double value
     */
    private Double extractDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            throw new IllegalArgumentException("Expected a numeric latitude/longitude");
        }
    }

    public static void main(String[] args) throws IOException {
        new RequestHandler().run();
    }
}
