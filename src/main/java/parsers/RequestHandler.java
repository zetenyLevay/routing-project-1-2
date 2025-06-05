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

public class RequestHandler {

    private final CLIRead cliRead;
    private final CLIWrite cliWrite;

    public RequestHandler() {
        this.cliRead  = new CLIRead();
        this.cliWrite = new CLIWrite();
    }

    public void run() throws IOException {
        System.err.println("Starting");

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

            @SuppressWarnings("unchecked")
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
                    ZipToSQLite.run(selectedFile);
                    cliWrite.sendOk("loaded");
                } catch (Exception e) {
                    cliWrite.sendError(e.getMessage());
                    break;
                }
                continue;
            }

            // --- routeFrom / to / startingAt ---
            if (request.containsKey("routeFrom")) {
                // 1) Check that routeFrom is a Map<?,?>
                Object routeFromObj = request.get("routeFrom");
                if (!(routeFromObj instanceof Map<?, ?>)) {
                    cliWrite.sendError("Bad request");
                    continue;
                }
                @SuppressWarnings("unchecked")
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
                @SuppressWarnings("unchecked")
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
                    System.out.println(
                        "SP: " + startPoint + 
                        "  EP: " + endPoint + 
                        "  ST: " + startingAtStr
                    );

                    RoutingEngineAstar router = new RoutingEngineAstar(
                        new DBConnectionManager("jdbc:sqlite:budapest_gtfs.db")
                    );
                    System.out.println("running engine");

                    List<RouteStep> route = router.findRoute(
                        startPoint.getLatitude(),
                        startPoint.getLongitude(),
                        endPoint.getLatitude(),
                        endPoint.getLongitude(),
                        startingAtStr
                    );
                    System.out.println("route found");
                    cliWrite.writeRouteSteps(route);

                } catch (Exception e) {
                    cliWrite.sendError(e.getMessage());
                }

                continue;
            }

            // --- anything else ---
            cliWrite.sendError("Bad request");
        }
    }

    /**
     * Helper: take an Object from JSON and coerce it to Double.
     * If it’s not a Number, throws IllegalArgumentException.
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
