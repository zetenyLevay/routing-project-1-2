package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter.Journey;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter.JourneyLeg;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.*;

/**
 * Reconstructs a journey from a search node, creating a sequence of journey legs.
 */
public class PathReconstructionService {

    /**
     * Represents a node in the search graph used for path reconstruction.
     */
    public static class SearchNode {
        /** The stop associated with this node. */
        public final DijkstraStop stop;
        /** The arrival time at this node in seconds since midnight. */
        public final int time;
        /** The previous node in the search path. */
        public final SearchNode previous;
        /** The connection used to reach this node. */
        public final DijkstraConnection connectionUsed;

        /**
         * Constructs a SearchNode with the specified details.
         *
         * @param stop           the stop associated with this node
         * @param time           the arrival time in seconds
         * @param previous       the previous node in the path
         * @param connectionUsed the connection used to reach this node
         */
        public SearchNode(DijkstraStop stop, int time, SearchNode previous, DijkstraConnection connectionUsed) {
            this.stop = stop;
            this.time = time;
            this.previous = previous;
            this.connectionUsed = connectionUsed;
        }
    }

    /**
     * Reconstructs a journey from the destination node back to the start.
     *
     * @param destinationNode the final node in the search path
     * @param departureTime   the departure time of the journey in seconds
     * @return a Journey object representing the reconstructed path
     */
    public Journey reconstructJourney(SearchNode destinationNode, int departureTime) {
        List<JourneyLeg> legs = buildJourneyLegs(destinationNode);
        Collections.reverse(legs);

        return new Journey(legs, departureTime, destinationNode.time);
    }

    /**
     * Builds a list of journey legs by backtracking from the destination node.
     *
     * @param destinationNode the final node in the search path
     * @return a list of JourneyLeg objects representing the journey
     */
    private List<JourneyLeg> buildJourneyLegs(SearchNode destinationNode) {
        List<JourneyLeg> legs = new ArrayList<>();
        SearchNode current = destinationNode;

        while (current.previous != null) {
            DijkstraConnection conn = current.connectionUsed;
            legs.add(new JourneyLeg(
                    conn.from, conn.to,
                    conn.departureTime, conn.arrivalTime,
                    conn.routeId, conn.tripId, conn.headSign,
                    "WALK".equals(conn.routeId)
            ));
            current = current.previous;
        }
        return legs;
    }
}