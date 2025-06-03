package closureAnalysis;

import closureAnalysis.calculations.CentralityCalculator;
import closureAnalysis.data.graph.StopEdge;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;
import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.models.NearbyPOIs;
import closureAnalysis.data.models.PointOfInterest;
import closureAnalysis.data.readers.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class StopEvaluator {

    CentralityCalculator cc = new CentralityCalculator();
    Finder coordFinder = new CoordinateFinder();
    POIFinder poiFinder = new POIFinder();
    TransportTypeFinder transportTypeFinder = new TransportTypeFinder();

    double ALPHA = 0.5;
    double GAMMA = 1.0;
    double OMEGA = 1.0;
    double BETA = 1.0;




    public static void main(String[] args) throws SQLException {
        StopEvaluator stopEvaluator = new StopEvaluator();
        StopGraph graph = new StopGraph();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:data/budapest_gtfs.db");
        long totalStart = System.currentTimeMillis();
        long sTime = System.currentTimeMillis();
        graph = graph.buildStopGraph(conn);
        long eTime = System.currentTimeMillis();
        long dTime = eTime - sTime;
        System.out.println("Building stop graph took :" + dTime + "ms");

        try (PrintWriter writer = new PrintWriter(new File("samples.csv"))){
            double[] weights = {0.0, 0.25, 0.5, 0.75, 1.0};

            for (double alpha : weights) {
                for (double beta : weights) {
                    for (double omega : weights) {
                        for (double gamma : weights) {
                            double sum = beta + omega + gamma;
                            if (sum == 0) continue;


                            double betaNorm = beta / sum;
                            double omegaNorm = omega / sum;
                            double gammaNorm = gamma / sum;

                            stopEvaluator.evaluate(graph, conn, alpha, betaNorm, omegaNorm, gammaNorm);

                            List<StopNode> sorted = graph.getStopNodes().stream().sorted(Comparator.comparing(StopNode::getStopWorth)).toList();

                            String top1 = sorted.get(0).getId();
                            String top2 = sorted.get(1).getId();
                            String top3 = sorted.get(2).getId();
                            String top4 = sorted.get(3).getId();
                            String top5 = sorted.get(4).getId();
                            String top6 = sorted.get(5).getId();
                            String top7 = sorted.get(6).getId();
                            String top8 = sorted.get(7).getId();
                            String top9 = sorted.get(8).getId();
                            String top10 = sorted.get(9).getId();

                            writer.printf(Locale.US,
                                    "%.2f,%.2f,%.2f,%.2f,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                                    alpha, betaNorm, omegaNorm, gammaNorm, top1, top2, top3, top4, top5, top6, top7, top8, top9, top10);

                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }





    }

    public void evaluate(StopGraph stopGraph, Connection conn, double alpha, double beta, double gamma, double omega) {
        Normalizer normalizer = new Normalizer();

        transportTypeFinder.preload(conn);
        poiFinder.preload();
        cc.calculateClosenessCentrality(stopGraph);
        cc.calculateBetweennessCentrality(stopGraph);
        normalizer.centralityNormalizer(stopGraph);
        List<StopNode> stops = stopGraph.getStopNodes();


        // USE ALL THE THREADS
        stops.parallelStream().forEach(stopNode -> {
            coordFinder.find(stopNode);
            poiFinder.find(stopNode);
            transportTypeFinder.find(stopNode);
            assignPoiValue(stopNode);
            assignTransportValue(stopNode);
            assignCentralityValue(stopNode, alpha);
        });

        normalizer.poiNormalizer(stopGraph);
        normalizer.transportNormalizer(stopGraph);

        stops.parallelStream().forEach(stopNode -> {
            stopEvaluation(stopNode, beta, gamma, omega);
        });
    }



    /**
     * using geometric mean the combined centrality measures of a stop (this punishes
     * @param node stop we are chekcing
     */
    private void assignCentralityValue(StopNode node, double alpha) {



        double closeness = node.getClosenessCentrality();
        double betweenness = node.getBetweennessCentrality();

        double value = (alpha * betweenness + (1-alpha) * closeness); // weighted sum
        double value2 = (Math.sqrt(closeness*betweenness)); // geometric mean

        if (node.getId().equals("105507")){
            System.out.println("stuff for stop: ");
            System.out.println(closeness);
            System.out.println(betweenness);
        }

        node.setCentralityWorth(value);

    }

    private void assignTransportValue(StopNode node) {

        TransportType transportType = node.getTransportType();

        double yearlyPassCount = transportType.yearlyPassengers;
        double stopCount = transportType.stopCount;

        double base = (yearlyPassCount/stopCount);



        node.setTransportWorth(base);

    }

    private void assignPoiValue(StopNode node) {
        NearbyPOIs a = node.getNearbyPOIs();
        double closeValue = 0;
        double farValue = 0;

        for (PointOfInterest poi : a.getClosePointsOfInterest()){

            closeValue += (double) (poi.getType().value) / 200;

        }

        for (PointOfInterest poi : a.getFarPointOfInterest()){

            farValue += (poi.getType().value * 0.5) / 250;

        }
        double totalValue = closeValue + farValue;

        node.setPoiWorth(totalValue);
    }

    private void stopEvaluation(StopNode node, double beta, double gamma, double omega) {
        double poiWorth = node.getPoiWorth();
        double centralityWorth = node.getCentralityWorth();
        double transportWorth = node.getTransportWorth();

        if (node.getId().equals("105507")){
            System.out.println("Poiworth: " + poiWorth + " Centrality: " + centralityWorth + " Transport: " + transportWorth);
        }

        double finalWorth = beta * poiWorth + omega * centralityWorth + gamma * transportWorth;

        node.setStopWorth(finalWorth);
    }
}
