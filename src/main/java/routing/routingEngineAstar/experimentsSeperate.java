package routing.routingEngineAstar;

import routing.db.DBConnectionManager;
import routing.routingEngineModels.RouteStep;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class experimentsSeperate {
    private static double sourceLat = 0.0;
    private static double sourceLon = 0.0;

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        String[] stopIdsToTest = {
                "118803",
                "F04562",
                "066687",
                "066686",
                "F04566",
                "009006",
                "F04392",
                "F04561",
                "066701",
                "F04559"
        };

        DBConnectionManager dbManager = new DBConnectionManager("jdbc:sqlite:budapest_gtfs.db");


        try (PrintWriter writer = new PrintWriter(new FileWriter("routing_results.csv"))) {

            writer.println("stopID,includedOrNot,rndPointID,destID,timeToGetTo");


            for (String stopIdToExclude : stopIdsToTest) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("STARTING TEST FOR STOP ID: " + stopIdToExclude);

                if (!getStopLocation(dbManager, stopIdToExclude)) {
                    System.err.println("stop not found in database: " + stopIdToExclude + ". skipping.");
                    continue;
                }

                System.out.println("Original coordinates for stop " + stopIdToExclude + ": Latitude=" + sourceLat + ", Longitude=" + sourceLon);
                String startTime = "18:11:11";

                String[] destNames = {
                        "Deák Ferenc Square",
                        "Keleti railway station",
                        "Kelenföld Metro/Railway station",
                        "Pestszentimre railway station",
                        "Rómaifürdő suburban railway station"
                };
                double[][] destCoords = {
                        {47.4924417, 19.0527917},
                        {47.50028, 19.08389},
                        {47.46444, 19.01861},
                        {47.40806, 19.18806},
                        {47.5520, 19.0310}
                };

                List<double[]> randomSources = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    randomSources.add(generateRandomCoord(sourceLat, sourceLon, 2000));
                }

                // first RUN 1: WITH the stop
                System.out.println("\n--- RUN 1: calculating routes WITH stop " + stopIdToExclude + " available ---");
                RoutingEngineAstar normalRouter = new RoutingEngineAstar(dbManager);
                runExperiment(writer, stopIdToExclude, "WITH", normalRouter, randomSources, destNames, destCoords, startTime);

                // secon RUN 2: WITHOUT the stop
                System.out.println("\n--- RUN 2: calculating routes WITHOUT stop " + stopIdToExclude + " ---");
                Set<String> excludedIds = Set.of(stopIdToExclude);
                RoutingEngineAstar excludingRouter = new RoutingEngineAstar(dbManager, excludedIds);
                runExperiment(writer, stopIdToExclude, "WITHOUT", excludingRouter, randomSources, destNames, destCoords, startTime);
            }

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n\nAll experiments finished. Results saved to routing_results.csv");
    }

    private static void runExperiment(PrintWriter writer, String stopId, String includedStatus, RoutingEngineAstar router, List<double[]> randomSources, String[] destNames, double[][] destCoords, String startTime) {
        String[] destIds = { "dest1", "dest2", "dest3", "dest4", "dest5" };

        for (int i = 0; i < randomSources.size(); i++) {
            double[] src = randomSources.get(i);
            int rndPointId = i + 1;
            String srcLabel = "rndPoint" + rndPointId + " (Lat: " + String.format("%.6f", src[0]) + ", Lon: " + String.format("%.6f", src[1]) + ")";
            System.out.println("From " + srcLabel + ":");
            for (int j = 0; j < destNames.length; j++) {
                double[] dest = destCoords[j];
                String destLabel = destNames[j];
                String destId = destIds[j];

                double time = calculateRouteTime(router, src[0], src[1], dest[0], dest[1], startTime);

                String timeString;
                String csvTimeString;

                if (time >= 0) {
                    timeString = String.format("%.2f minutes", time);
                    csvTimeString = String.format("%.2f", time);
                } else {
                    timeString = "No route found";
                    csvTimeString = "-1.0";
                }

                System.out.println("  To " + destLabel + ": " + timeString);

                writer.printf("%s,%s,%d,%s,%s%n",
                        stopId,
                        includedStatus,
                        rndPointId,
                        destId,
                        csvTimeString
                );
            }
            System.out.println();
        }
    }

    private static boolean getStopLocation(DBConnectionManager dbManager, String stopId) {
        String query = "SELECT stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, stopId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                sourceLat = rs.getDouble("stop_lat");
                sourceLon = rs.getDouble("stop_lon");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static double[] generateRandomCoord(double centerLat, double centerLon, double radiusMeters) {
        double u = Math.random();
        double theta = 2 * Math.PI * Math.random();
        double d = radiusMeters * Math.sqrt(u);
        double latRad = Math.toRadians(centerLat);
        double deltaLat = (d * Math.cos(theta)) / 111320.0;
        double deltaLon = (d * Math.sin(theta)) / (111320.0 * Math.cos(latRad));
        double newLat = centerLat + deltaLat;
        double newLon = centerLon + deltaLon;
        return new double[]{newLat, newLon};
    }

    private static double calculateRouteTime(RoutingEngineAstar router, double srcLat, double srcLon, double dstLat, double dstLon, String startTime) {
        List<RouteStep> route = router.findRoute(srcLat, srcLon, dstLat, dstLon, startTime);
        if (route.isEmpty()) {
            return -1;
        } else {
            double totalDurationMinutes = 0;
            for (RouteStep step : route) {
                totalDurationMinutes += step.getNumOfMinutes();
            }
            return totalDurationMinutes;
        }
    }
}