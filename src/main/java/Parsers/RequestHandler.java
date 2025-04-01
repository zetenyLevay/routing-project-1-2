package Parsers;

import java.io.EOFException;
import java.io.IOException;
import java.util.Map;

import com.leastfixedpoint.json.JSONSyntaxError;

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
                }
                // ... process other requests here
                //the switch statement deciding which transporation to use.
                //load 
                //make ping pong like test cases
                //be able to convert from json to 
                //you need to turn the json into java objects
                //make bash scripts , msys2 for windows, git bash
                // run basic hello worlds file containing {"ping":kkf}

                //CI gitlab actions to run tests automatically when pushing, check if theyre enabled.
                //java tests
            }
            cliWrite.sendError("Bad request");
        }
    }

    /**
     * Main method to run the request handler.
     *
     * @param args command-line arguments.
     * @throws IOException if an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        new RequestHandler().run();
    }
}
