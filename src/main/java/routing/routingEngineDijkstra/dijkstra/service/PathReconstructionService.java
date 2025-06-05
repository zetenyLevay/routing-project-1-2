package routing.routingEngineDijkstra.dijkstra.service;

import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter.Journey;
import routing.routingEngineDijkstra.dijkstra.algorithm.DijkstraRouter.JourneyLeg;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraConnection;
import routing.routingEngineDijkstra.dijkstra.model.input.DijkstraStop;

import java.util.*;

public class PathReconstructionService {

    public static class SearchNode {
        public final DijkstraStop stop;
        public final int time;
        public final SearchNode previous;
        public final DijkstraConnection connectionUsed;

        public SearchNode(DijkstraStop stop, int time, SearchNode previous, DijkstraConnection connectionUsed) {
            this.stop = stop;
            this.time = time;
            this.previous = previous;
            this.connectionUsed = connectionUsed;
        }
    }

    public Journey reconstructJourney(SearchNode destinationNode, int departureTime) {
        List<JourneyLeg> legs = buildJourneyLegs(destinationNode);
        Collections.reverse(legs);

        return new Journey(legs, departureTime, destinationNode.time);
    }



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