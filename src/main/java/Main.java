
import java.io.IOException;

import gui.MapUI;
import parsers.RequestHandler;

public class Main {

    public static void main(String[] args) throws IOException {
        RequestHandler requestHandler = new RequestHandler();
        
        MapUI.create();
        requestHandler.run();

    }
    //BudaPest:
    // {"routeFrom":{"lat":47.51828032904577,"lon":18.97828487843043},"to":{"lat":47.4924417,"lon":19.0527917},"startingAt":"18:54:00"}
    //Budapest: 
    //{"routeFrom":{"lat":47.533834,"lon":18.862378},"to":{"lat":47.4924417,"lon":19.0527917},"startingAt":"18:54:00"}

    //Budapest super edgecase:
    //{"routeFrom":{"lat":47.567101,"lon":18.909322},"to":{"lat":47.568488,"lon":19.275036},"startingAt":"18:54:00"}
    //{"load":"data/budapest_gtfs.zip"}
}
