import java.io.IOException;

import parsers.RequestHandler;

public class Main {
    

    public static void main(String[] args) throws IOException {
        RequestHandler requestHandler = new RequestHandler();

        // MapUI.create();
        requestHandler.run();

    }

    // {"routeFrom":{"lat":47.51828032904577,"lon":18.97828487843043},"to":{"lat":47.4924417,"lon":19.0527917},"startingAt":"18:54:00"}
}
