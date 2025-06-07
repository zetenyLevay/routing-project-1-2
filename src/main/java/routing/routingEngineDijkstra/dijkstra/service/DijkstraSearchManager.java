package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.service.PathReconstructionService.SearchNode;

import java.util.*;

public class DijkstraSearchManager {
    private final PriorityQueue<SearchNode> queue;
    private final Map<String, Integer> earliestArrival;
    private final Set<String> processedStops;

    public DijkstraSearchManager() {
        this.queue = new PriorityQueue<>(512, Comparator.comparingInt(node -> node.time));
        this.earliestArrival = new HashMap<>(512);
        this.processedStops = new HashSet<>(512);
    }

    public void initialize(DijkstraStop startStop, int departureTime) {
        SearchNode startNode = new SearchNode(startStop, departureTime, null, null);
        queue.add(startNode);
        earliestArrival.put(startStop.id, departureTime);
    }

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

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int getEarliestArrival(DijkstraStop stop) {
        return earliestArrival.getOrDefault(stop.id, Integer.MAX_VALUE);
    }
}