import java.io.IOException;

import parsers.RequestHandler;

public class Main {
    

    public static void main(String[] args) throws IOException {
        RequestHandler requestHandler = new RequestHandler();

        requestHandler.run();
        
    }
}
