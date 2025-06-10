
import java.io.IOException;

import parsers.RequestHandler;

public class Main {

    /**
     * Main method used to launch the Request Handler application. This
     * application handles requests for routing and GTFS data processing.
     *
     * @param args command line arguments (not used)
     * @throws IOException if there is an error processing requests
     */
    public static void main(String[] args) throws IOException {
        RequestHandler requestHandler = new RequestHandler();
        requestHandler.run();
    }

}
