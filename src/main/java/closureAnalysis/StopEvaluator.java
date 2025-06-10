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
import java.util.*;
/**
 * The StopEvaluator class is responsible for evaluating the worth or importance of transit stops
 * based on multiple factors including centrality measures, nearby points of interest, and transport types.
 * It combines these factors using weighted sums to calculate a final stop worth value.
 *
 * <p>The evaluation process involves:
 * <ul>
 *   <li>Building a stop graph from GTFS data</li>
 *   <li>Calculating centrality measures (closeness and betweenness)</li>
 *   <li>Finding nearby points of interest for each stop</li>
 *   <li>Determining transport types for each stop</li>
 *   <li>Normalizing and combining these factors</li>
 * </ul>
 *
 * <p>The class uses several weighting parameters (ALPHA, BETA, GAMMA, OMEGA) to control
 * the influence of different factors in the final evaluation.
 */
public class StopEvaluator {

    CentralityCalculator cc = new CentralityCalculator();
    Finder coordFinder = new CoordinateFinder();

    double ALPHA = 0.5;
    double GAMMA = 0.33;
    double OMEGA = 0.33;
    double BETA = 0.33;


    /**
     * Performs the complete stop evaluation process.
     * @return A map of stop IDs to their calculated worth values
     */
    public Map<String, Double> doEverything(){
        StopGraph graph = new StopGraph();
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data/june2ndBudapestGTFS.db");
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    /**
     * Preprocesses the stop graph by calculating centrality measures,
     * finding coordinates, POIs, transport types, and names for each stop.
     * @param stopGraph The graph to preprocess
     * @param transportTypeFinder Finder for transport types
     * @param poiFinder Finder for points of interest
     * @param nameFinder Finder for stop names
     */
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
    /**
     * Assigns a combined centrality value to each stop using a weighted sum
     * of closeness and betweenness centrality.
     * @param stops List of stops to evaluate
     * @param alpha Weight for betweenness centrality (closeness weight is 1-alpha)
     */
    private void assignCentralityValue(List<StopNode> stops, double alpha) {

        stops.parallelStream().forEach(node -> {
            double closeness = node.getClosenessCentrality();
            double betweenness = node.getBetweennessCentrality();
            double value = alpha * betweenness + (1 - alpha) * closeness;
            node.setCentralityWorth(value);
        });

    }

    /**
     * Calculates and assigns a transport worth value based on the stop's transport type.
     * @param node The stop node to evaluate
     */
    private void assignTransportValue(StopNode node) {

        TransportType transportType = node.getTransportType();

        double yearlyPassCount = transportType.yearlyPassengers;
        double stopCount = transportType.stopCount;

        double base = (yearlyPassCount/stopCount);



        node.setTransportWorth(base);

    }

    /**
     * Calculates and assigns a POI worth value based on nearby points of interest.
     * @param node The stop node to evaluate
     */
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

    /**
     * Performs the final stop evaluation by combining normalized POI, centrality,
     * and transport worth values using the specified weights.
     * @param graph The stop graph to evaluate
     * @param beta Normalized weight for POI worth
     * @param gamma Normalized weight for transport worth
     * @param omega Normalized weight for centrality worth
     */
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
