package gui;

import java.io.IOException;
import java.sql.SQLException;

import parsers.ZipToSQLite;

public class MainUI {

    /**
     * Main method used to launch the Map UI application. NOTE: the database has
     * to be loaded in order for this to work
     *
     * @param args command line arguments (not used)
     * @throws IOException if there is an error loading the map data
     * @throws SQLException
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) throws IOException, SQLException {
        ZipToSQLite zts = new ZipToSQLite();
        System.out.println("Loading databse...");
        zts.run("data/budapest_gtfs.zip");
        System.out.println("Database loaded successfully");
        MapUI.create();
    }
}
