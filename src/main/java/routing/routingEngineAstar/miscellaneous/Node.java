package routing.routingEngineAstar.miscellaneous;

import routing.routingEngineModels.Stop.Stop;

public class Node implements Comparable<Node> {

        public final Stop stop;
        public final int arrivalTime;   // seconds since midnight
        final double fScore;

        public Node(Stop stop, int arrivalTime, double fScore) {
            this.stop = stop;
            this.arrivalTime = arrivalTime;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }