package closureAnalysis;

import closureAnalysis.calculations.CentralityCalculator;
import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;
import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.models.NearbyPOIs;
import closureAnalysis.data.models.PointOfInterest;
import closureAnalysis.data.readers.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class StopEvaluator {

    CentralityCalculator cc = new CentralityCalculator();
    Finder coordFinder = new CoordinateFinder();
    POIFinder poiFinder = new POIFinder();
    TransportTypeFinder transportTypeFinder = new TransportTypeFinder();



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

        stopEvaluator.evaluate(graph, conn);

        long totalEnd = System.currentTimeMillis();
        long totalTime = totalEnd - totalStart;
        long totalTimeNoGraph = totalTime - dTime;

        System.out.println("Evaluation took :" + totalTime + "ms");
        System.out.println("Total time without building graph: " + totalTimeNoGraph + "ms");

        System.out.println("Bottom ten:");
        graph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getStopWorth))
                .limit(10)
                .forEach(node -> System.out.println("Node : " + node.getStopWorth() + "Label: " + node.getId()));
        System.out.println("Top ten:");
        graph.getStopNodes().stream()
                .sorted(Comparator.comparingDouble(StopNode::getStopWorth).reversed())
                .limit(10)
                .forEach(node -> System.out.println("Node : " + node.getStopWorth() + "Label: " + node.getId()));
    }

    public void evaluate(StopGraph stopGraph, Connection conn) throws SQLException {

        long preloadTime = System.currentTimeMillis();
        transportTypeFinder.preload(conn);
        poiFinder.preload();
        long eTime = System.currentTimeMillis();
        long dTime = eTime - preloadTime;
        System.out.println("Preload time: " + dTime + "ms");

        System.out.println("Starting closeness calculation...");
        long closenessStart = System.currentTimeMillis();
        cc.calculateClosenessCentrality(stopGraph);
        long closenessEnd = System.currentTimeMillis();
        long closenessduration = closenessEnd - closenessStart;
        System.out.println("Finished closeness: " + closenessduration + "ms");

        System.out.println("Starting Betweenness calculation...");
        long betweennessStart = System.currentTimeMillis();
        cc.calculateBetweennessCentrality(stopGraph);
        long betweennessEnd = System.currentTimeMillis();
        long betweennessduration = betweennessEnd - betweennessStart;
        System.out.println("Finished betweenness: " + betweennessduration + "ms");

        System.out.println("Starting normalization...");
        long normalizationStart = System.currentTimeMillis();
        centralityNormalizer(stopGraph);
        long normalizationEnd = System.currentTimeMillis();
        long normalizationduration = normalizationEnd - normalizationStart;
        System.out.println("Finished normalization: " + normalizationduration + "ms");

        int count = 0;

        System.out.println("Starting Evaluation...");
        long startTime = System.currentTimeMillis();


        List<StopNode> stops = stopGraph.getStopNodes();

        stops.parallelStream().forEach(stopNode -> {
            coordFinder.find(stopNode);
            poiFinder.find(stopNode);
            transportTypeFinder.find(stopNode);
            transportTypeEvaluator(stopNode);
            poiEvaluator(stopNode);
            centralityEvaluator(stopNode);
        });

/*
        for (StopNode stopNode : stopGraph.getStopNodes()) {

            //long coordStart = System.currentTimeMillis();
            coordFinder.find(stopNode);
            //long coordEnd = System.currentTimeMillis();
           // long coordDuration = coordEnd - coordStart;
           // System.out.println("Coord took: " + coordDuration + " ms");
            //long poiStart = System.currentTimeMillis();
            poiFinder.find(stopNode);
           // long poiEnd = System.currentTimeMillis();
           // long poiDuration = poiEnd - poiStart;
           // System.out.println("Poi took: " + poiDuration + " ms");
            //long transportStart = System.currentTimeMillis();
            transportTypeFinder.find(stopNode);
           // long transportEnd = System.currentTimeMillis();
            //long transportDuration = transportEnd - transportStart;
           // System.out.println("Transport took: " + transportDuration + " ms");

           // long transPortEvaluatorStart = System.currentTimeMillis();
            transportTypeEvaluator(stopNode);
            //long transPortEvaluatorEnd = System.currentTimeMillis();
            //long transPortEvaluatorDuration = transPortEvaluatorEnd - transPortEvaluatorStart;
           // System.out.println("Transport Evaluation took: " + transPortEvaluatorDuration + " ms");
            //long poiEvaluatorStart = System.currentTimeMillis();
            poiEvaluator(stopNode);
            //long poiEvaluatorEnd = System.currentTimeMillis();
           // long poiEvaluatorDuration = poiEvaluatorEnd - poiEvaluatorStart;
           // System.out.println("Poi Evaluation took: " + poiEvaluatorDuration + " ms");
           // long centralityCalculatorStart = System.currentTimeMillis();
            centralityEvaluator(stopNode);
            //long centralityCalculatorEnd = System.currentTimeMillis();
           // long centralityCalculatorDuration = centralityCalculatorEnd - centralityCalculatorStart;
           // System.out.println("Centrality took: " + centralityCalculatorDuration + " ms");
           // count++;
           // System.out.println("Nodes done: " + count);





            //System.out.println("This stops: " + stopNode.getId() +  " worth: "  + stopNode.getStopWorth() );

        }

 */


        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Finished evaluation in " + elapsedTime + " ms");
    }

    // we do a min-max normalization on centrality
    private void centralityNormalizer(StopGraph stopGraph) {
        double maxCloseness = stopGraph.getStopNodes().stream()
                .max(Comparator.comparingDouble(StopNode::getClosenessCentrality))
                .map(StopNode::getClosenessCentrality)
                .orElse(Double.NaN);
        double maxBetweenness = stopGraph.getStopNodes().stream()
                .max(Comparator.comparingDouble(StopNode::getBetweennessCentrality))
                .map(StopNode::getBetweennessCentrality)
                .orElse(Double.NaN);
        double minCloseness = stopGraph.getStopNodes().stream()
                .min(Comparator.comparingDouble(StopNode::getClosenessCentrality))
                .map(StopNode::getClosenessCentrality)
                .orElse(Double.NaN);
        double minBetweenness = stopGraph.getStopNodes().stream()
                .min(Comparator.comparingDouble(StopNode::getBetweennessCentrality))
                .map(StopNode::getBetweennessCentrality)
                .orElse(Double.NaN);

        for (StopNode stopNode : stopGraph.getStopNodes()) {
            double closeness = stopNode.getClosenessCentrality();
            double betweenness = stopNode.getBetweennessCentrality();

            double normalizedCloseness = (closeness - minCloseness) / (maxCloseness - minCloseness);
            double normalizedBetweenness = (betweenness - minBetweenness) / (maxBetweenness - minBetweenness);

            stopNode.setClosenessCentrality(normalizedCloseness);
            stopNode.setBetweennessCentrality(normalizedBetweenness);
        }

    }

    private void centralityEvaluator(StopNode node) {

        double ALPHA = 0.75;

        double worth = node.getStopWorth();
        double closeness = node.getClosenessCentrality();
        double betweenness = node.getBetweennessCentrality();

        double value = (ALPHA * betweenness + (1-ALPHA) * closeness) * 100; // weighted sum
        double value2 = (Math.sqrt(closeness*betweenness))*100; // geometric mean

        //System.out.println("Centrality value: " + value);

        worth += value;
        node.setStopWorth(worth);

    }

    private void transportTypeEvaluator(StopNode node) {
        double worth = node.getStopWorth();
        TransportType transportType = node.getTransportType();

        double yearlyPassCount = transportType.yearlyPassengers;
        double stopCount = transportType.stopCount;

        double base = (yearlyPassCount/stopCount) * 100;

        //System.out.println("Transport type value: " + base);

        worth += base;

        node.setStopWorth(worth);

    }

    private void poiEvaluator(StopNode node) {
        NearbyPOIs a = node.getNearbyPOIs();

        double worth = node.getStopWorth();
        double closeValue = 0;
        double farValue = 0;

        for (PointOfInterest poi : a.getClosePointsOfInterest()){

            closeValue += (double) (poi.getType().value) / 200;

        }

        for (PointOfInterest poi : a.getFarPointOfInterest()){

            farValue += (poi.getType().value * 0.5) / 250;

        }

        //System.out.println("POI value close: " + closeValue);
        //System.out.println("POI value far: " + farValue);

        worth += farValue;
        worth += closeValue;
        node.setStopWorth(worth);
    }
}
