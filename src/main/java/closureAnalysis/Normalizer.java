package closureAnalysis;

import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

import java.util.Comparator;

/**
 * Provides normalization functionality for various node metrics using min-max scaling.
 *
 * <p>Normalization is performed to bring different metrics to a common scale (0-1)
 * for fair comparison and weighted combination. Supported normalizations include:
 * <ul>
 *   <li>Centrality measures (closeness and betweenness)</li>
 *   <li>Point of Interest (POI) worth values</li>
 *   <li>Transport worth values</li>
 * </ul>
 */
public class Normalizer {
    /**
     * Normalizes centrality measures (closeness and betweenness) using min-max scaling.
     * @param stopGraph The graph containing nodes to normalize
     */
    public void centralityNormalizer(StopGraph stopGraph) {
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
    /**
     * Normalizes POI worth values using min-max scaling.
     * @param stopGraph The graph containing nodes to normalize
     */
    public void poiNormalizer(StopGraph stopGraph) {
        double maxPoi = stopGraph.getStopNodes().stream()
                .max(Comparator.comparingDouble(StopNode::getPoiWorth))
                .map(StopNode::getPoiWorth)
                .orElse(Double.NaN);
        double minPoi = stopGraph.getStopNodes().stream()
                .min(Comparator.comparingDouble(StopNode::getPoiWorth))
                .map(StopNode::getPoiWorth)
                .orElse(Double.NaN);
        for (StopNode stopNode : stopGraph.getStopNodes()) {
            double poiWorth = stopNode.getPoiWorth();
            double normalizedPoiWorth = (poiWorth - minPoi) / (maxPoi - minPoi);

            stopNode.setPoiWorth(normalizedPoiWorth);
        }
    }
    /**
     * Normalizes transport worth values using min-max scaling.
     * @param stopGraph The graph containing nodes to normalize
     */
    public void transportNormalizer(StopGraph stopGraph) {
        double maxTransport = stopGraph.getStopNodes().stream()
                .max(Comparator.comparingDouble(StopNode::getTransportWorth))
                .map(StopNode::getTransportWorth)
                .orElse(Double.NaN);
        double minTransport = stopGraph.getStopNodes().stream()
                .min(Comparator.comparingDouble(StopNode::getTransportWorth))
                .map(StopNode::getTransportWorth)
                .orElse(Double.NaN);
        for (StopNode stopNode : stopGraph.getStopNodes()) {
            double transportWorth = stopNode.getTransportWorth();
            double normalizedTransportWorth = (transportWorth - minTransport) / (maxTransport - minTransport);

            stopNode.setTransportWorth(normalizedTransportWorth);
        }
    }
}
