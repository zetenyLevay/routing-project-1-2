package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.dijkstra.service.PathReconstructionService.SearchNode;

import java.util.*;

public class DijkstraSearchManager {
    private final PriorityQueue<SearchNode> queue;
    private final Map<DijkstraStop, Integer> earliestArrival;
    private final Map<DijkstraStop, SearchNode> nodeMap; // NEW for optimization

    public DijkstraSearchManager() {
        this.queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.time));
        this.earliestArrival = new HashMap<>();
        this.nodeMap = new HashMap<>();
    }

    public void initialize(DijkstraStop startStop, int departureTime) {
        SearchNode startNode = new SearchNode(startStop, departureTime, null, null);
        queue.add(startNode);
        earliestArrival.put(startStop, departureTime);
        nodeMap.put(startStop, startNode);
    }

    public SearchNode getNextNode() {
        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();
            //lazy deletion check
            if (current.time <= earliestArrival.getOrDefault(current.stop, Integer.MAX_VALUE)) {
                return current;
            }
        }
        return null;
    }

    public boolean tryAddNode(SearchNode newNode) {
        int currentBest = earliestArrival.getOrDefault(newNode.stop, Integer.MAX_VALUE);
        if (newNode.time < currentBest) {
            // Remove existing node if present for cleaner queue
            SearchNode existing = nodeMap.get(newNode.stop);
            if (existing != null) {
                queue.remove(existing);
            }

            earliestArrival.put(newNode.stop, newNode.time);
            queue.add(newNode);
            nodeMap.put(newNode.stop, newNode);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
    public int getEarliestArrival(DijkstraStop stop) {
        return earliestArrival.getOrDefault(stop, Integer.MAX_VALUE);
    }
}