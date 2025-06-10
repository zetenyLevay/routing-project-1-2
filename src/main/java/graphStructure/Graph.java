package graphStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import routing.routingEngineDijkstra.adiModels.AdiRouteStep;
import routing.routingEngineDijkstra.adiModels.Stop.AdiStop;

/**
 * Graph will be a A hashmap linking Stop and a root step. where a journey will
 * be a list of steps
 *
 */
public class Graph {

    // Maps each Stop to the list of outgoing RouteSteps
    private final Map<AdiStop, List<AdiRouteStep>> graph = new HashMap<>();

    /**
     * Adds a stop (node) to the graph if not already present.
     *
     * @param stop The AdiStop to be added.
     */
    public void addStop(AdiStop stop) {
        graph.putIfAbsent(stop, new ArrayList<>());
    }

    /**
     * Adds a RouteStep (edge) originating from the given stop. Ensures the
     * 'from' stop exists in the graph.
     *
     * @param from The AdiStop from which the RouteStep originates.
     * @param step The AdiRouteStep to be added as an outgoing edge. If the
     * 'from' stop does not exist, it will be added automatically.
     */
    public void addRouteStep(AdiStop from, AdiRouteStep step) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(step);
    }

    /**
     * Retrieves all RouteSteps (edges) outgoing from the given stop.
     *
     * * @param stop The AdiStop for which to retrieve outgoing RouteSteps.
     * @return An unmodifiable list of AdiRouteStep objects representing the
     * outgoing edges.
     */
    public List<AdiRouteStep> getRouteStepsFrom(AdiStop stop) {
        return Collections.unmodifiableList(
                graph.getOrDefault(stop, List.of())
        );
    }

    /**
     * Returns all stops (nodes) in the graph.
     *
     * @return An unmodifiable list of AdiStop objects representing all stops in
     * the graph.
     */
    public List<AdiStop> getStops() {
        return Collections.unmodifiableList(new ArrayList<>(graph.keySet()));
    }

    /**
     * Checks if the graph contains the specified stop.
     *
     * @param stop The AdiStop to check for existence in the graph.
     * @return true if the stop exists in the graph, false otherwise.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Graph:\n");
        for (var entry : graph.entrySet()) {
            sb.append("Stop ")
                    .append(entry.getKey().getStopID())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }
        return sb.toString();
    }

}
