package gui;

import parsers.RequestHandler;

import java.io.IOException;

public class MainUI {

    public static void main(String[] args) throws IOException {
        //uncomment at some point
               MapUI.create();
        RequestHandler rh = new RequestHandler();
        rh.run();
    }
}