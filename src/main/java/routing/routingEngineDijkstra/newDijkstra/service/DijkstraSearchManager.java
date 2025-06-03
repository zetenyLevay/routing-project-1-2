package routing.routingEngineDijkstra.newDijkstra.service;

import routing.routingEngineDijkstra.newDijkstra.model.input.DijkstraStop;
import routing.routingEngineDijkstra.newDijkstra.service.PathReconstructionService.SearchNode;

import java.util.*;

public class DijkstraSearchManager {
    private final PriorityQueue<SearchNode> queue;
    private final Map<DijkstraStop, Integer> earliestArrival;

    public DijkstraSearchManager() {
        this.queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.time));
        this.earliestArrival = new HashMap<>();
    }



        public void initialize(DijkstraStop startStop, int departureTime) {
        SearchNode startNode = new SearchNode(startStop, departureTime, null, null);
        queue.add(startNode);
        earliestArrival.put(startStop, departureTime);
    }

    public SearchNode getNextNode() {
        while (!queue.isEmpty()) {
            SearchNode current = queue.poll();
            if (current.time <= earliestArrival.getOrDefault(current.stop, Integer.MAX_VALUE)) {
                return current;
            }


        }
        return null;
    }


    public boolean tryAddNode(SearchNode newNode) {
        int currentBest = earliestArrival.getOrDefault(newNode.stop, Integer.MAX_VALUE);
        if (newNode.time < currentBest) {
            earliestArrival.put(newNode.stop, newNode.time);
            queue.add(newNode);
            return true;
        }
        return false;
    }



    public boolean isEmpty() {
        return queue.isEmpty();
    }
}