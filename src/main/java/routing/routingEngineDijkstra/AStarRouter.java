package routing.routingEngineDijkstra;

import routing.routingEngineCSA.engine.util.StraightLineCalculator;
import routing.routingEngineModels.*;
import java.time.LocalTime;
import java.util.*;

public class AStarRouter {
    static class Node implements Comparable<Node> {
        StopDijkstra stop;
        int currentTime;
        double fScore;
        FinalRoute route;

        public Node(StopDijkstra stop, int currentTime, double fScore, FinalRoute route) {
            this.stop = stop;
            this.currentTime = currentTime;
            this.fScore = fScore;
            this.route = route;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    public static FinalRoute findShortestPath(
            Map<String, StopDijkstra> stops,
            Map<String, List<Connection>> connections,
            StopDijkstra startStop,
            StopDijkstra endStop,
            LocalTime departureTime) {

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, Integer> gScore = new HashMap<>();
        Coordinates endCoords = new Coordinates(endStop.getLatitude(), endStop.getLongitude());

        int departureTimeSec = departureTime.toSecondOfDay();
        double initialH = calculateHeuristic(startStop, endCoords);

        FinalRoute initialRoute = new FinalRoute(new ArrayList<>(), 0, 0);
        Node startNode = new Node(startStop, departureTimeSec, departureTimeSec + initialH, initialRoute);

        openSet.add(startNode);
        gScore.put(startStop.getStopID(), departureTimeSec);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.stop.getStopID().equals(endStop.getStopID())) {
                return current.route;
            }

            for (Connection conn : connections.getOrDefault(current.stop.getStopID(), Collections.emptyList())) {
                if (conn.departureTime >= current.currentTime) {
                    StopDijkstra toStop = stops.get(conn.toStopId);
                    int newArrivalTime = conn.arrivalTime;

                    if (newArrivalTime < gScore.getOrDefault(toStop.getStopID(), Integer.MAX_VALUE)) {
                        Coordinates startCoord = new Coordinates(current.stop.getLatitude(), current.stop.getLongitude());
                        Coordinates endCoord = new Coordinates(toStop.getLatitude(), toStop.getLongitude());
                        double travelTime = (newArrivalTime - current.currentTime) / 60.0;

                        RouteStep step = new RouteStep(
                                "TRANSIT-" + conn.tripId,
                                startCoord,
                                endCoord,
                                travelTime
                        );

                        ArrayList<RouteStep> newSteps = new ArrayList<>(current.route.getRouteSteps());
                        newSteps.add(step);

                        double distance = current.route.getTotalDistance() +
                                StraightLineCalculator.haversineDistanceMeters(current.stop, endCoord);
                        double totalTime = current.route.getTotalTime() + travelTime;

                        FinalRoute newRoute = new FinalRoute(newSteps, distance, totalTime);

                        double h = calculateHeuristic(toStop, endCoords);
                        Node newNode = new Node(toStop, newArrivalTime, newArrivalTime + h, newRoute);

                        gScore.put(toStop.getStopID(), newArrivalTime);
                        openSet.add(newNode);
                    }
                }
            }

            for (StopDijkstra neighborStop : stops.values()) {
                if (neighborStop.getStopID().equals(current.stop.getStopID())) continue;

                if (current.stop.isPlatform() && neighborStop.isPlatform() &&
                        Objects.equals(current.stop.getParentStationID(), neighborStop.getParentStationID())) {
                    continue;
                }

                int walkTimeSec = StraightLineCalculator.calculateWalkingTimeSeconds(
                        current.stop,
                        new Coordinates(neighborStop.getLatitude(), neighborStop.getLongitude())
                );

                if (walkTimeSec > 1800) continue;

                int walkArrivalTime = current.currentTime + walkTimeSec;
                if (walkArrivalTime < gScore.getOrDefault(neighborStop.getStopID(), Integer.MAX_VALUE)) {
                    Coordinates startCoord = new Coordinates(current.stop.getLatitude(), current.stop.getLongitude());
                    Coordinates endCoord = new Coordinates(neighborStop.getLatitude(), neighborStop.getLongitude());
                    double walkTimeMin = walkTimeSec / 60.0;

                    RouteStep step = new RouteStep("WALK", startCoord, endCoord, walkTimeMin);
                    ArrayList<RouteStep> newSteps = new ArrayList<>(current.route.getRouteSteps());
                    newSteps.add(step);

                    double distance = current.route.getTotalDistance() +
                            StraightLineCalculator.haversineDistanceMeters(current.stop, endCoord);
                    double totalTime = current.route.getTotalTime() + walkTimeMin;

                    FinalRoute newRoute = new FinalRoute(newSteps, distance, totalTime);

                    double h = calculateHeuristic(neighborStop, endCoords);
                    Node newNode = new Node(neighborStop, walkArrivalTime, walkArrivalTime + h, newRoute);

                    gScore.put(neighborStop.getStopID(), walkArrivalTime);
                    openSet.add(newNode);
                }
            }
        }
        return null;
    }

    private static double calculateHeuristic(StopDijkstra fromStop, Coordinates endCoords) {
        return StraightLineCalculator.calculateWalkingTimeSeconds(fromStop, endCoords);
    }

    static class Connection {
        String fromStopId;
        String toStopId;
        String tripId;
        int departureTime;
        int arrivalTime;

        public Connection(String fromStopId, String toStopId, String tripId, int departureTime, int arrivalTime) {
            this.fromStopId = fromStopId;
            this.toStopId = toStopId;
            this.tripId = tripId;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }
    }
}
