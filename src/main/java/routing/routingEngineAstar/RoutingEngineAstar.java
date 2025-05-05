package routing.routingEngineAstar;

import routing.routingEngineModels.FinalRoute;
import routing.routingEngineModels.InputJourney;


import routing.routingEngineModels.*;
import routing.routingenginemain.model.Stop;
import routing.routingenginemain.model.Coordinates;
import routing.routingenginemain.model.pathway.Pathway;

import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;


public class RoutingEngineAstar {

    //Receive object called Journey in format Journey{StartCoordinates, EndCoordinates, StartTime}
    //Output list of objects (FinalRoute) of type RouteStep, where trips is {Mode of transport, StartPoint, EndPoint, StartTime}

    // we want dijksta to return finalroute


/**
 * Implements A* search over a transit network (GTFS-based) with bus schedules and walking transfers.
 */
public class AStarRoutingEngine {
    private final Map<String, Stop> stops = new HashMap<>();
    private final Map<String, List<ServiceEdge>> serviceEdgesByStop = new HashMap<>();
    private final double walkingSpeedKmPerHour = 5.0;
    private final double maxBusSpeedKmPerHour = 50.0;

    /**
     * Initialize engine by loading stops, pathways, and service edges from SQLite GTFS.
     * @param sqliteDbPath path to SQLite file (e.g. "gtfs.db")
     */
    public AStarRoutingEngine(String sqliteDbPath) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteDbPath);
        loadStops(conn);
        loadServiceEdges(conn);
        conn.close();
    }

    private void loadStops(Connection conn) throws SQLException {
        // 1) load all stops with empty footpaths
        String sqlStops = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlStops)) {
            while (rs.next()) {
                String id = rs.getString("stop_id");
                String name = rs.getString("stop_name");
                double lat = rs.getDouble("stop_lat");
                double lon = rs.getDouble("stop_lon");
                Stop s = new Stop(id, name, new Coordinates(lat, lon), new ArrayList<>());
                stops.put(id, s);
            }
        }
        // 2) load pathways and attach to stops
        String sqlPaths = "SELECT pathway_id, from_stop_id, to_stop_id, pathway_mode, traversal_time, is_bidirectional FROM pathways";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlPaths)) {
            while (rs.next()) {
                String pid = rs.getString("pathway_id");
                String from = rs.getString("from_stop_id");
                String to   = rs.getString("to_stop_id");
                int code    = rs.getInt("pathway_mode");
                int timeSec = rs.getInt("traversal_time");
                Stop sFrom = stops.get(from);
                Stop sTo   = stops.get(to);
                if (sFrom != null && sTo != null) {
                    Pathway p = new Pathway(pid, sFrom, sTo, code, timeSec);
                    sFrom.getFootpaths().add(p);
                    // if bidirectional, also add reverse
                    if (rs.getInt("is_bidirectional") == 1) {
                        Pathway rev = new Pathway(pid + "_rev", sTo, sFrom, code, timeSec);
                        sTo.getFootpaths().add(rev);
                    }
                }
            }
        }
    }

    private void loadServiceEdges(Connection conn) throws SQLException {
        // load every consecutive segment of trips as a scheduled edge
        String sql = "SELECT st1.trip_id, st1.stop_id AS from_id, st1.departure_time AS dep, " +
                     "st2.stop_id AS to_id, st2.arrival_time AS arr " +
                     "FROM stop_times st1 " +
                     "JOIN stop_times st2 " +
                     "ON st1.trip_id = st2.trip_id AND st2.stop_sequence = st1.stop_sequence + 1";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String from   = rs.getString("from_id");
                String to     = rs.getString("to_id");
                String depStr = rs.getString("dep");
                String arrStr = rs.getString("arr");
                Stop sFrom = stops.get(from);
                Stop sTo   = stops.get(to);
                if (sFrom == null || sTo == null || depStr == null || arrStr == null) continue;
                LocalTime dep = LocalTime.parse(depStr);
                LocalTime arr = LocalTime.parse(arrStr);
                double travelMin = Duration.between(dep, arr).toMinutes();
                ServiceEdge e = new ServiceEdge(sFrom, sTo, tripId, dep, arr, travelMin);
                serviceEdgesByStop
                    .computeIfAbsent(from, k -> new ArrayList<>())
                    .add(e);
            }
        }
        // sort edges by departure time for fast "next" lookup
        for (List<ServiceEdge> list : serviceEdgesByStop.values()) {
            list.sort(Comparator.comparing(e -> e.departureTime));
        }
    }

    /**
     * Find fastest path (min total minutes) from journey.start to journey.end.
     */
    public FinalRoute findRoute(InputJourney journey) {
        // snap to nearest stops
        Stop startStop = findNearestStop(journey.getStart());
        Stop endStop   = findNearestStop(journey.getEnd());
        double walkInitial = computeWalkingMinutes(journey.getStart(), startStop.getLatitude(), startStop.getLongitude());
        double walkFinal   = computeWalkingMinutes(journey.getEnd(), endStop.getLatitude(),   endStop.getLongitude());
        LocalTime startTimeAtStop = journey.getStartTime().plusSeconds((long)(walkInitial * 60));

        // A* state: (stop, time)
        class State {
            final Stop stop;
            final LocalTime time;
            State(Stop s, LocalTime t) { stop = s; time = t; }
            @Override public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof State)) return false;
                State s = (State) o;
                return stop.getStopID().equals(s.stop.getStopID()) && time.equals(s.time);
            }
            @Override public int hashCode() {
                return Objects.hash(stop.getStopID(), time);
            }
        }

        // edge record to reconstruct path
        Map<State, Object> viaEdge = new HashMap<>(); // maps state -> ServiceEdge or Pathway
        Map<State, State> cameFrom = new HashMap<>();
        Map<State, Double> gScore = new HashMap<>();
        Map<State, Double> fScore = new HashMap<>();

        // comparator for open set nodes
        class SearchNode implements Comparable<SearchNode> {
            final State state; final double f;
            SearchNode(State state, double f) { this.state = state; this.f = f; }
            @Override public int compareTo(SearchNode o) { return Double.compare(this.f, o.f); }
        }

        PriorityQueue<SearchNode> open = new PriorityQueue<>();
        State startState = new State(startStop, startTimeAtStop);
        gScore.put(startState, walkInitial);
        double h0 = heuristic(startStop, journey.getEnd());
        fScore.put(startState, walkInitial + h0);
        open.offer(new SearchNode(startState, fScore.get(startState)));

        State goalState = null;
        while (!open.isEmpty()) {
            SearchNode node = open.poll();
            State cur = node.state;
            // skip stale
            double curF = gScore.get(cur) + heuristic(cur.stop, journey.getEnd());
            if (node.f > curF) continue;
            if (cur.stop.getStopID().equals(endStop.getStopID())) {
                goalState = cur;
                break;
            }
            double curG = gScore.get(cur);

            // 1) footpaths
            for (Pathway p : cur.stop.getFootpaths()) {
                Stop nbr = p.getToStop();
                long walkSec = p.getTraversalTime();
                LocalTime nbrTime = cur.time.plusSeconds(walkSec);
                double cost = curG + walkSec / 60.0;
                State nbrState = new State(nbr, nbrTime);
                if (cost < gScore.getOrDefault(nbrState, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(nbrState, cur);
                    viaEdge.put(nbrState, p);
                    gScore.put(nbrState, cost);
                    double f = cost + heuristic(nbr, journey.getEnd());
                    fScore.put(nbrState, f);
                    open.offer(new SearchNode(nbrState, f));
                }
            }

            // 2) service edges (bus)
            List<ServiceEdge> edges = serviceEdgesByStop.getOrDefault(cur.stop.getStopID(), Collections.emptyList());
            // binary search first departure >= cur.time
            int idx = Collections.binarySearch(edges, null, Comparator.comparing((ServiceEdge e) -> e.departureTime)
                .thenComparing(e -> e.to.stopID));
            // workaround: find manually first
            idx = 0;
            while (idx < edges.size() && edges.get(idx).departureTime.isBefore(cur.time)) idx++;
            for (; idx < edges.size(); idx++) {
                ServiceEdge e = edges.get(idx);
                LocalTime dep = e.departureTime;
                LocalTime arr = e.arrivalTime;
                double waitMin = Duration.between(cur.time, dep).toMinutes();
                double rideMin = e.travelMinutes;
                double cost = curG + waitMin + rideMin;
                State nbrState = new State(e.to, arr);
                if (cost < gScore.getOrDefault(nbrState, Double.POSITIVE_INFINITY)) {
                    cameFrom.put(nbrState, cur);
                    viaEdge.put(nbrState, e);
                    gScore.put(nbrState, cost);
                    double f = cost + heuristic(e.to, journey.getEnd());
                    fScore.put(nbrState, f);
                    open.offer(new SearchNode(nbrState, f));
                }
            }
        }

        if (goalState == null) {
            return new FinalRoute(new ArrayList<>(), 0, Double.POSITIVE_INFINITY);
        }

        // reconstruct path
        List<Object> edgePath = new LinkedList<>();
        List<State> statePath = new LinkedList<>();
        State cur = goalState;
        statePath.add(0, cur);
        while (!cur.equals(startState)) {
            Object edge = viaEdge.get(cur);
            edgePath.add(0, edge);
            cur = cameFrom.get(cur);
            statePath.add(0, cur);
        }

        // build RouteSteps
        List<RouteStep> steps = new ArrayList<>();
        // initial walk
        steps.add(new RouteStep("WALK", journey.getStart(), new Coordinates(
                statePath.get(0).stop.getLatitude() + "," + statePath.get(0).stop.getLongitude()), walkInitial));
        double totalDist = 0, totalTime = 0;
        for (int i = 1; i < statePath.size(); i++) {
            State prev = statePath.get(i-1);
            State now  = statePath.get(i);
            Object edge = edgePath.get(i-1);
            if (edge instanceof Pathway) {
                Pathway p = (Pathway) edge;
                double tmin = p.getTraversalTime() / 60.0;
                Coordinates a = new Coordinates(
                    prev.stop.getLatitude() + "," + prev.stop.getLongitude());
                Coordinates b = new Coordinates(
                    now.stop.getLatitude() + "," + now.stop.getLongitude());
                steps.add(new RouteStep("WALK", a, b, tmin));
                totalTime += tmin;
                totalDist += haversineDist(a, b);
            } else if (edge instanceof ServiceEdge) {
                ServiceEdge e = (ServiceEdge) edge;
                // wait
                long waitSec = Duration.between(prev.time, e.departureTime).getSeconds();
                double waitMin = waitSec / 60.0;
                Coordinates loc = new Coordinates(
                    prev.stop.getLatitude() + "," + prev.stop.getLongitude());
                steps.add(new RouteStep("WAIT", loc, loc, waitMin));
                totalTime += waitMin;
                // ride
                double rideMin = e.travelMinutes;
                Coordinates fromC = new Coordinates(
                    prev.stop.getLatitude() + "," + prev.stop.getLongitude());
                Coordinates toC = new Coordinates(
                    now.stop.getLatitude() + "," + now.stop.getLongitude());
                steps.add(new RouteStep("BUS", fromC, toC, rideMin));
                totalTime += rideMin;
                totalDist += haversineDist(fromC, toC);
            }
        }
        // final walk
        steps.add(new RouteStep("WALK", new Coordinates(
                endStop.getLatitude() + "," + endStop.getLongitude()), journey.getEnd(), walkFinal));
        totalTime += walkFinal;
        totalDist += haversineDist(new Coordinates(
                endStop.getLatitude() + "," + endStop.getLongitude()), journey.getEnd());

        return new FinalRoute((ArrayList<RouteStep>) steps, totalDist, totalTime);
    }

    private double heuristic(Stop s, Coordinates goal) {
        double d = haversineDist(
            new Coordinates(s.getLatitude() + "," + s.getLongitude()), goal);
        // time = dist (km) / speed (km/h) * 60
        return (d / maxBusSpeedKmPerHour) * 60.0;
    }

    private Stop findNearestStop(Coordinates coord) {
        Stop best = null;
        double bestD = Double.POSITIVE_INFINITY;
        for (Stop s : stops.values()) {
            double d = haversineDist(coord,
                new Coordinates(s.getLatitude() + "," + s.getLongitude()));
            if (d < bestD) {
                bestD = d;
                best = s;
            }
        }
        return best;
    }

    private double computeWalkingMinutes(Coordinates a, double lat, double lon) {
        Coordinates b = new Coordinates(lat + "," + lon);
        double dist = haversineDist(a, b); // km
        // time (min) = dist(km)/speed(km/h)*60
        return (dist / walkingSpeedKmPerHour) * 60.0;
    }

    private static double haversineDist(Coordinates c1, Coordinates c2) {
        // returns kilometers
        double R = 6371.0;
        double lat1 = Math.toRadians(c1.getLatitude());
        double lon1 = Math.toRadians(c1.getLongitude());
        double lat2 = Math.toRadians(c2.getLatitude());
        double lon2 = Math.toRadians(c2.getLongitude());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                 + Math.cos(lat1)*Math.cos(lat2)
                 * Math.sin(dLon/2)*Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    // inner class for scheduled edges
    private static class ServiceEdge {
        final Stop from, to;
        final String tripId;
        final LocalTime departureTime;
        final LocalTime arrivalTime;
        final double travelMinutes;
        ServiceEdge(Stop f, Stop t, String tripId,
                    LocalTime dep, LocalTime arr, double mins) {
            this.from = f; this.to = t;
            this.tripId = tripId;
            this.departureTime = dep;
            this.arrivalTime   = arr;
            this.travelMinutes = mins;
        }
    }
}






    public FinalRoute run(InputJourney journey) {
        return null;

    }

}
