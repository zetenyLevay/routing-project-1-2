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

    double ALPHA = 0.5;
    double GAMMA = 0.33;
    double OMEGA = 0.33;
    double BETA = 0.33;



    public Map<String, Double> doEverything(){
        StopGraph graph = new StopGraph();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:data/june2ndBudapestGTFS.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        graph = graph.buildStopGraph(conn);


        TransportTypeFinder transportTypeFinder = new TransportTypeFinder();
        POIFinder poiFinder = new POIFinder();
        NameFinder nameFinder = new NameFinder();
        transportTypeFinder.preload(conn);
        poiFinder.preload();
        nameFinder.preload(conn);


        preprocess(graph, transportTypeFinder, poiFinder, nameFinder);
        assignCentralityValue(graph.getStopNodes(), ALPHA);
        double sum = BETA + GAMMA + OMEGA;

        double betaNorm = BETA / sum;
        double omegaNorm = OMEGA / sum;
        double gammaNorm = GAMMA / sum;

        stopEvaluation(graph, betaNorm, omegaNorm, gammaNorm);

        List<StopNode> stopNodes = graph.getStopNodes();

        Map<String, Double> stopValues = new HashMap<>();
        for (StopNode stopNode : stopNodes) {
            stopValues.putIfAbsent(stopNode.getId(), stopNode.getStopWorth());
        }
        return stopValues;
    }


    public static void main(String[] args) throws SQLException {
        StopEvaluator stopEvaluator = new StopEvaluator();
        Map<String, Double> stopValues = stopEvaluator.doEverything();
        System.out.println(stopValues);
    }

    public void exportStopScores(StopGraph graph, String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.println("stop_id,poiWorth,centralityWorth,transportWorth");
            for (StopNode node : graph.getStopNodes()) {
                writer.printf(Locale.US, "%s,%.4f,%.4f,%.4f\n",
                        node.getId(),
                        node.getPoiWorth(),
                        node.getCentralityWorth(),
                        node.getTransportWorth()
                );
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void preprocess(StopGraph stopGraph, TransportTypeFinder transportTypeFinder, POIFinder poiFinder, NameFinder nameFinder) {
        Normalizer normalizer = new Normalizer();


        cc.calculateClosenessCentrality(stopGraph);
        cc.calculateBetweennessCentrality(stopGraph);
        normalizer.centralityNormalizer(stopGraph);
        List<StopNode> stops = stopGraph.getStopNodes();

        // USE ALL THE THREADS
        stops.parallelStream().forEach(stopNode -> {
            coordFinder.find(stopNode);
            poiFinder.find(stopNode);
            transportTypeFinder.find(stopNode);
            nameFinder.find(stopNode);
            assignPoiValue(stopNode);
            assignTransportValue(stopNode);
        });

        normalizer.poiNormalizer(stopGraph);
        normalizer.transportNormalizer(stopGraph);
    }

    public void evaluate(StopGraph stopGraph, double alpha, double beta, double gamma, double omega,
                         TransportTypeFinder transportTypeFinder, POIFinder poiFinder) {



    }



    /**
     * using geometric mean the combined centrality measures of a stop (this punishes
     * @param stops stop we are chekcing
     */
    private void assignCentralityValue(List<StopNode> stops, double alpha) {

        stops.parallelStream().forEach(node -> {
            double closeness = node.getClosenessCentrality();
            double betweenness = node.getBetweennessCentrality();
            double value = alpha * betweenness + (1 - alpha) * closeness;
            node.setCentralityWorth(value);
        });

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

    private void stopEvaluation(StopGraph graph, double beta, double gamma, double omega) {
        List<StopNode> stops = graph.getStopNodes();



        stops.parallelStream().forEach(stopNode -> {
            assignPoiValue(stopNode);
            assignTransportValue(stopNode);
        });

        Normalizer normalizer = new Normalizer();
        normalizer.transportNormalizer(graph);
        normalizer.poiNormalizer(graph);



        stops.parallelStream().forEach(node -> {
            double poiWorth = node.getPoiWorth();
            double centralityWorth = node.getCentralityWorth();
            double transportWorth = node.getTransportWorth();

            double finalWorth = beta * poiWorth + omega * centralityWorth + gamma * transportWorth;

            node.setStopWorth(finalWorth);
        });
    }
}
