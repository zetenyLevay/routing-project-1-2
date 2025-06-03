package closureAnalysis.data.readers;

import closureAnalysis.data.graph.StopNode;

/**
 * lots of finders, have a random interface for it
 */
public interface Finder {
    void find(StopNode input);
}
