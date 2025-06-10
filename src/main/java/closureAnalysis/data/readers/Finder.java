package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;

/**
 * Interface defining the contract for finding and attaching additional information to StopNodes.
 * Implementations of this interface are responsible for retrieving and setting specific data attributes
 * (coordinates, names, POIs, transport types) to stop nodes in the transit graph.
 */
public interface Finder {
    /**
     * Finds and attaches relevant information to the given StopNode.
     * @param input The StopNode to be enriched with additional data
     */
    void find(StopNode input);
}
