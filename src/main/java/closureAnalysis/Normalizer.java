package closureAnalysis;

import closureAnalysis.data.graph.StopGraph;
import closureAnalysis.data.graph.StopNode;

import java.util.Comparator;

public class Normalizer {
    /**
     * min-max normalization on centrality (might do this with the others)
     * @param stopGraph
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
