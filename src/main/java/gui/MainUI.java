package gui;

import java.io.IOException;

import parsers.ZipToSQLite;

public class MainUI {

    public static void main(String[] args) throws IOException {
        //uncomment at some point
        //   MapUI.create();

        //{"load":"/Users/jakelockitch/Downloads/budapest_gtfs.zip"}
        //{"load":"budapest_gtfs.zip"}
        //{"load":"data/budapest_gtfs.zip"}
        //NOTE: you have to be explicit about the path to the GTFS file
        //    RequestHandler rh = new RequestHandler();
        //    rh.run();
        ZipToSQLite zts = new ZipToSQLite();
        zts.run("data/budapest_gtfs.zip");
    }
}
