package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.service.PathReconstructionService.SearchNode;

import java.util.*;

/**
 * Manages the search process for Dijkstra's algorithm, handling node prioritization and tracking of earliest arrival times.
 */
public class DijkstraSearchManager {
    private final PriorityQueue<SearchNode> queue;
    private final Map<String, Integer> earliestArrival;
    private final Set<String> processedStops;

    /**
     * Constructs a DijkstraSearchManager with initialized data structures for search management.
     */
    public DijkstraSearchManager() {
        this.queue = new PriorityQueue<>(512, Comparator.comparingInt(node -> node.time));
        this.earliestArrival = new HashMap<>(512);
        this.processedStops = new HashSet<>(512);
    }

    /**
     * Initializes the search with the starting stop and departure time.
     *
     * @param startStop    the starting stop
     * @param departureTime the departure time in seconds since midnight
     */
    public void initialize(DijkstraStop startStop, int departureTime) {
        SearchNode startNode = new SearchNode(startStop, departureTime, null, null);
        queue.add(startNode);
        earliestArrival.put(startStop.id, departureTime);
    }

    /**
     * Retrieves the next node to process from the priority queue, ensuring it hasn't been processed and has an optimal arrival time.
     *
     * @return the next valid SearchNode to process, or null if no valid nodes remain
     */
    public SearchNode getNextNode() {
        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();

            if (processedStops.contains(current.stop.id)) {
                continue;
            }

            if (current.time <= earliestArrival.getOrDefault(current.stop.id, Integer.MAX_VALUE)) {
                processedStops.add(current.stop.id);
                return current;
            }
        }
        return null;
    }

    /**
     * Attempts to add a new node to the search queue if it improves the arrival time for its stop.
     *
     * @param newNode the node to add
     * @return true if the node was added, false if it was not (e.g., already processed or not optimal)
     */
    public boolean tryAddNode(SearchNode newNode) {
        String stopId = newNode.stop.id;

        if (processedStops.contains(stopId)) {
            return false;
        }

        int currentBest = earliestArrival.getOrDefault(stopId, Integer.MAX_VALUE);
        if (newNode.time < currentBest) {
            earliestArrival.put(stopId, newNode.time);
            queue.add(newNode);
            return true;
        }
        return false;
    }

    /**
     * Checks if the search queue is empty.
     *
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Retrieves the earliest known arrival time for a given stop.
     *
     * @param stop the stop to check
     * @return the earliest arrival time in seconds, or Integer.MAX_VALUE if unknown
     */
    public int getEarliestArrival(DijkstraStop stop) {
        return earliestArrival.getOrDefault(stop.id, Integer.MAX_VALUE);
    }
}