import java.io.IOException;

import gui.MapUI;
import parsers.RequestHandler;

public class Main {
    

    public static void main(String[] args) throws IOException {
        RequestHandler requestHandler = new RequestHandler();

        MapUI.create();
        requestHandler.run();

    }
}
