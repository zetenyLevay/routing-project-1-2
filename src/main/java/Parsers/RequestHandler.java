package Parsers;

import java.io.EOFException;
import java.io.IOException;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.leastfixedpoint.json.JSONSyntaxError;

import Routing.RoutingEngineModels.Coordinate;

public class RequestHandler {

    private CLIRead cliRead;
    private CLIWrite cliWrite;

    public RequestHandler() {
        cliRead = new CLIRead();
        cliWrite = new CLIWrite();
    }

    //TODO: needs to be able to speak to the routing engine

    /**
     * Processes incoming JSON requests in a loop.
     *
     * @throws IOException if an I/O error occurs.
     */
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

            if (json instanceof Map<?, ?>) {
                Map<?, ?> request = (Map<?, ?>) json;

                if (request.containsKey("ping")) {
                    cliWrite.sendOk(Map.of("pong", request.get("ping")));
                    continue;
                } else if (request.containsKey("load")) {
                    String selectedFile = (String) request.get("load");

                    //Unzip(selectedFile); 
                    //TODO: hook up to ZipToSQLite
                }

                //TODO: double check this is how it works?
                else if (request.containsKey("routeFrom")) {

                    Coordinate startPoint = new Coordinate((String) request.get("routeFrom"));
                    Coordinate endPoint = new Coordinate((String) request.get("to"));
                    String startingAtStr = (String) request.get("startingAt");

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("H:mm");
                    LocalTime startingAtTime = LocalTime.parse(startingAtStr, fmt);

                    // System.out.println(request.get("routeFrom"));
                    System.out.println("SP: " + startPoint.toString() + " EP: " + endPoint.toString() + " ST: " + startingAtTime);
                    // // {"routeFrom": "41.40338, 2.17403","to": "41.4032, 2.1283","startingAt": "10:05"}
                    // InputJourney journey = new InputJourney(startPoint, endPoint, startingTime);

                    // Dijkstra.run(journey);
                    // CSA.run(journey); 
                } else {
                    cliWrite.sendError("Bad request");
                }


                // ... process other requests here
                //the switch statement deciding which transporation to use.
                //load 
                //make ping pong like test cases
                //be able to convert from json to 
                //you need to turn the json into java objects
                //make bash scripts , msys2 for windows, git bash
                // run basic hello worlds file containing {"ping":kkf}


                /**
                 * {"load":filenameString}
                 * {"routeFrom":sourcePoint,"to":targetPoint,"startingAt":timeString}
                 * Anything else gives an error
                 * {"error": error message}





                 */

                //CI gitlab actions to run tests automatically when pushing, check if theyre enabled.
                //java tests
            }

        }
    }

    // /**
    //  * Main method to run the request handler.
    //  *
    //  * @param args command-line arguments.
    //  * @throws IOException if an I/O error occurs.
    //  */
    public static void main(String[] args) throws IOException {
        new RequestHandler().run();
    }
}
