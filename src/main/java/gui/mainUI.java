package gui;

import Parsers.RequestHandler;

import java.io.IOException;

public class mainUI {

    public static void main(String[] args) throws IOException {
        //uncomment at some point
        //        MapUI.create();
        RequestHandler rh = new RequestHandler();
        rh.run();
    }
}