package routing.routingEngineAstar;


import routing.routingEngineModels.Stop.Stop;
import routing.routingEngineModels.aStarModel.AStarNode;
import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.RouteInfo;
import routing.routingEngineModels.RouteStep;
import routing.routingEngineModels.utils.TimeAndGeoUtils;

import java.sql.*;
import java.util.*;

/**
 * AStarRouter runs a time‐dependent A* on a GTFS‐style database.
 * 
 * Usage:
 *   AStarRouter router = new AStarRouter("jdbc:sqlite:/path/to/transit.db");
 *   List<RouteStep> steps = router.findRoute(
 *        sourceLat, sourceLon,
 *        destLat, destLon,
 *        "13:45:00"
 *   );
 */
public class RoutingEngineAstar {
    //test

    /** How many seconds of waiting at most we allow for boarding a trip. */
    private static final int MAX_WAIT_SECONDS = 3600; // 1 hour

    /** The maximum radius (in meters) we walk from the origin to “first stops.” */
    private static final double INITIAL_WALK_RADIUS_M = 1000.0; // 1 km

    /** The maximum distance (in meters) we allow as a “footpath transfer” between two stops. */
    private static final double FOOTPATH_RADIUS_M = 500.0; // 0.5 km

    /** JDBC connection (SQLite, PostgreSQL, etc.) */
    private final String jdbcUrl;

    /** Cache of all stops: stopID → Stop object (with lat/lon, name, etc.) */
    private final Map<String, Stop> allStops;

    /** Precomputed “footpath” adjacency: stopID → List of (neighborStopID, walkingSeconds) */
    private final Map<String, List<Footpath>> footpathAdj;

    /**
     * A small helper class for walking transfers between two stops.
     */
    private static class Footpath {
        final String neighborStopID;
        final int walkSeconds;
        Footpath(String neighborStopID, int walkSeconds) {
            this.neighborStopID = neighborStopID;
            this.walkSeconds     = walkSeconds;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2) Constructor: open DB and load all stops & build footpaths
    // ──────────────────────────────────────────────────────────────────────────

    public RoutingEngineAstar(String jdbcUrl) throws SQLException {
        this.jdbcUrl = jdbcUrl;

        // 2.1) Load all stops into memory
        this.allStops = loadAllStops();

        // 2.2) Build a “footpath adjacency” for all stops within FOOTPATH_RADIUS_M
        this.footpathAdj = buildFootpathAdjacency();
    }

    /**
     * Connect to the database and do:
     *   SELECT stop_id, stop_name, stop_lat, stop_lon
     *   FROM stops;
     * for every row, construct a Stop object.
     */
    private Map<String, Stop> loadAllStops() throws SQLException {
        Map<String, Stop> map = new HashMap<>();
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            String sql = "SELECT stop_id, stop_name, stop_lat, stop_lon, location_type, parent_station FROM stops";
            // Note: I added location_type & parent_station columns in case your Stop constructor needs them.
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String stopID   = rs.getString("stop_id");
                String stopName = rs.getString("stop_name");
                double lat      = rs.getDouble("stop_lat");
                double lon      = rs.getDouble("stop_lon");
                int locationType = rs.getInt("location_type");      // 0 = stop/platform, etc.
                String parentStation = rs.getString("parent_station"); // maybe null

                // You said your Stop constructor is:
                //   new Stop(String stopID, String stopName, Coordinates coords, int code, String parentID)
                Coordinates coords = new Coordinates(lat, lon);
                Stop s = new Stop(stopID, stopName, coords, locationType, parentStation);
                map.put(stopID, s);
            }
        }
        return map;
    }

    /**
     * Build a simple O(N^2) walk‐adjacency for stops that lie within FOOTPATH_RADIUS_M.
     */
    private Map<String, List<Footpath>> buildFootpathAdjacency() {
        Map<String, List<Footpath>> adj = new HashMap<>();
        List<Stop> stopList = new ArrayList<>(allStops.values());

        for (int i = 0; i < stopList.size(); i++) {
            Stop si = stopList.get(i);
            String id_i = si.getStopID();
            adj.putIfAbsent(id_i, new ArrayList<>());

            for (int j = i + 1; j < stopList.size(); j++) {
                Stop sj = stopList.get(j);
                String id_j = sj.getStopID();

                double d = TimeAndGeoUtils.haversineMeters(
                    si.getLatitude(), si.getLongitude(),
                    sj.getLatitude(), sj.getLongitude()
                );
                if (d <= FOOTPATH_RADIUS_M) {
                    int walkSec = TimeAndGeoUtils.walkingTimeSeconds(d);
                    adj.get(id_i).add(new Footpath(id_j, walkSec));
                    adj.putIfAbsent(id_j, new ArrayList<>());
                    adj.get(id_j).add(new Footpath(id_i, walkSec));
                }
            }
        }
        return adj;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3) The public API: findRoute(...)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns a chronological list of RouteStep from (lat0,lon0) @ startTimeStr
     * to (lat1,lon1), using walking at 5 km/h and public transit from the DB.
     *
     * If no transit route is found, this will at least walk‐only from source to dest
     * (if within a reasonable radius), or return an empty list if too far.
     */
    public List<RouteStep> findRoute(
        double sourceLat, double sourceLon,
        double destLat,   double destLon,
        String startTimeStr
    ) throws SQLException {

        int startSec = TimeAndGeoUtils.timeStringToSeconds(startTimeStr);
        Stop dummyDestStop = new Stop("DEST", "DESTINATION", new Coordinates(destLat, destLon), 0, null);

        // 3.1) Find all “startable” stops within INITIAL_WALK_RADIUS_M of the source point.
        //       Each one yields an initial node: (stopID, startSec + walkSec) with gScore = walkSec.
        List<InitialNode> initialNodes = findStopsWithinRadius(sourceLat, sourceLon, INITIAL_WALK_RADIUS_M, startSec);

        // 3.2) A* data structures:
        //   - openSet: a PQ of (fScore, counter, AStarNode)
        //   - gScore: best cost found so far: Map<AStarNode, Integer>
        //   - cameFrom: Map<AStarNode, CameFromEntry>
        PriorityQueue<QueueEntry> openSet = new PriorityQueue<>();
        Map<AStarNode, Integer> gScore = new HashMap<>();
        Map<AStarNode, CameFromEntry> cameFrom = new HashMap<>();
        int counter = 0;

        // 3.3) Initialize openSet with each “initial walking” node
        for (InitialNode in : initialNodes) {
            AStarNode node = new AStarNode(in.stop, in.arrivalSec);
            gScore.put(node, in.walkSec);
            int h = heuristic(in.stop, destLat, destLon);
            int f = in.walkSec + h;
            openSet.add(new QueueEntry(f, counter++, node));

            // We record that “we came from” a special SOURCE node ⇒ this walk step
            // We’ll treat (“SOURCE”, startSec) as a sentinel parent.
            RouteStep walkStep = new RouteStep(
                "walk",
                in.stop.getStopID(), 
                (int) Math.ceil(in.walkSec / 60.0),
                startTimeStr,
                null, // stopName (N/A when walking TO a stop)
                null  // routeInfo
            );
            cameFrom.put(node, new CameFromEntry(new AStarNode(new Stop("SOURCE","SRC", new Coordinates(sourceLat, sourceLon), 0, null), startSec), walkStep));
        }

        // 3.4) Keep track of the best “walk‐directly‐to‐dest” solution seen so far
        int bestFinalCost = Integer.MAX_VALUE;
        AStarNode bestFinalNode = null;
        RouteStep bestFinalWalkStep = null;

        // 3.5) Main A* Loop
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            while (!openSet.isEmpty()) {
                QueueEntry qe = openSet.poll();
                int fCurrent = qe.fScore;
                AStarNode current = qe.node;
                int gCurrent = gScore.get(current);

                // If this fCurrent is ≥ bestFinalCost, we cannot do better
                if (fCurrent >= bestFinalCost) {
                    break;
                }

                Stop currStop = current.getStop();
                int currArrSec = current.getTimeSec();

                // 3.5a) Check if “walk from current stop → final destination” is better
                double dToDest = TimeAndGeoUtils.haversineMeters(
                    currStop.getLatitude(), currStop.getLongitude(),
                    destLat, destLon
                );
                int walkSecToDest = TimeAndGeoUtils.walkingTimeSeconds(dToDest);
                int totalIfWalk = gCurrent + walkSecToDest;
                if (totalIfWalk < bestFinalCost) {
                    bestFinalCost = totalIfWalk;
                    bestFinalNode = current;
                    // Build one final “walk” RouteStep
                    RouteStep finalWalk = new RouteStep(
                        "walk",
                        "DEST",                               // special “destination” sentinel
                        (int) Math.ceil(walkSecToDest / 60.0),// minutes
                        TimeAndGeoUtils.secondsToTimeString(currArrSec),
                        null, // no stopName since it goes TO lat/lon
                        null  // no routeInfo
                    );
                    bestFinalWalkStep = finalWalk;
                }

                // 3.5b) Expand “ride” neighbors from (currStop, currArrSec)
                List<RideNeighbor> rides = getRideNeighbors(conn, currStop.getStopID(), currArrSec);
                for (RideNeighbor rn : rides) {
                    int departureSec = rn.departureSec;
                    int arrivalSec   = rn.arrivalSec;
                    String nextStopID = rn.nextStopID;

                    Stop nextStop = allStops.get(nextStopID);
                    if (nextStop == null) continue; // sanity check

                    // The A* “g” value if we board this trip: 
                    //    total seconds from startSec until arrivalSec
                    int candidateG = arrivalSec - startSec;
                    AStarNode neighbor = new AStarNode(nextStop, arrivalSec);

                    Integer prevBest = gScore.get(neighbor);
                    if (prevBest == null || candidateG < prevBest) {
                        gScore.put(neighbor, candidateG);
                        int h = heuristic(nextStop, destLat, destLon);
                        int fNew = candidateG + h;
                        openSet.add(new QueueEntry(fNew, counter++, neighbor));

                        // Record “cameFrom”:
                        // We boarded at departureSec, rode until arrivalSec to nextStop
                        RouteInfo rinfo = new RouteInfo(
                            rn.routeID,
                            rn.routeShortName,
                            rn.routeLongName,
                            rn.routeType
                        );
                        int rideMinutes = (int) Math.ceil((arrivalSec - departureSec) / 60.0);
                        RouteStep rideStep = new RouteStep(
                            "ride",
                            nextStopID,
                            rideMinutes,
                            TimeAndGeoUtils.secondsToTimeString(departureSec),
                            allStops.get(nextStopID).getStopName(),
                            rinfo
                        );
                        cameFrom.put(neighbor, new CameFromEntry(current, rideStep));
                    }
                }

                // 3.5c) Expand “footpath” (walking‐transfer) neighbors, if any
                List<Footpath> fpList = footpathAdj.getOrDefault(currStop.getStopID(), Collections.emptyList());
                for (Footpath fp : fpList) {
                    String nbrID = fp.neighborStopID;
                    int walkSec = fp.walkSeconds;
                    int candidateArr = currArrSec + walkSec;
                    int candidateG  = candidateArr - startSec;
                    Stop nbrStop = allStops.get(nbrID);
                    AStarNode neighbor = new AStarNode(nbrStop, candidateArr);

                    // Prune if this candidate is already no better than bestFinalCost
                    if (candidateG < bestFinalCost) {
                        Integer prevBest = gScore.get(neighbor);
                        if (prevBest == null || candidateG < prevBest) {
                            gScore.put(neighbor, candidateG);
                            int h = heuristic(nbrStop, destLat, destLon);
                            int fNew = candidateG + h;
                            openSet.add(new QueueEntry(fNew, counter++, neighbor));

                            // Record “cameFrom” as a walk step
                            int walkMinutes = (int) Math.ceil(walkSec / 60.0);
                            RouteStep walkStep = new RouteStep(
                                "walk",
                                nbrID,
                                walkMinutes,
                                TimeAndGeoUtils.secondsToTimeString(currArrSec),
                                null,
                                null
                            );
                            cameFrom.put(neighbor, new CameFromEntry(current, walkStep));
                        }
                    }
                }
            }
        } // end try‐with‐resources (DB)

        // 3.6) If we never set bestFinalNode, no route was found
        if (bestFinalNode == null) {
            // We can fallback to “walk entire way” if distance is reasonable:
            double directDist = TimeAndGeoUtils.haversineMeters(sourceLat, sourceLon, destLat, destLon);
            int directWalkSec = TimeAndGeoUtils.walkingTimeSeconds(directDist);
            if (directWalkSec < MAX_WAIT_SECONDS * 2) {
                // Return a single “walk” RouteStep
                RouteStep onlyWalk = new RouteStep(
                    "walk",
                    "DEST",
                    (int) Math.ceil(directWalkSec / 60.0),
                    startTimeStr,
                    null,
                    null
                );
                return List.of(onlyWalk);
            } else {
                return Collections.emptyList();
            }
        }

        // 3.7) Reconstruct path by walking backward from bestFinalNode
        List<RouteStep> result = new ArrayList<>();
        // (a) Insert the final walk to destination
        result.add(bestFinalWalkStep);

        // (b) Walk backward via cameFrom until the “SOURCE” sentinel is reached
        AStarNode cur = bestFinalNode;
        while (true) {
            CameFromEntry cfe = cameFrom.get(cur);
            if (cfe == null) break; // safety
            AStarNode parent = cfe.getParent();
            RouteStep step = cfe.getStep();

            // If parent’s stopID == "SOURCE", we include this step then break
            if (parent.getStop().getStopID().equals("SOURCE")) {
                result.add(step);
                break;
            } else {
                result.add(step);
                cur = parent;
            }
        }

        // 3.8) Reverse so that the earliest step is first
        Collections.reverse(result);
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 4) Helper: find all stops within radius of (lat, lon).  Return (Stop, walkSec).
    // ──────────────────────────────────────────────────────────────────────────
    private static class InitialNode {
        final Stop stop;
        final int   walkSec;
        final int   arrivalSec;
        InitialNode(Stop stop, int walkSec, int arrivalSec) {
            this.stop = stop;
            this.walkSec = walkSec;
            this.arrivalSec = arrivalSec;
        }
    }

    private List<InitialNode> findStopsWithinRadius(
        double lat0, double lon0, double radiusMeters, int startSec
    ) {
        List<InitialNode> result = new ArrayList<>();
        for (Stop s : allStops.values()) {
            double d = TimeAndGeoUtils.haversineMeters(
                lat0, lon0,
                s.getLatitude(), s.getLongitude()
            );
            if (d <= radiusMeters) {
                int wsec = TimeAndGeoUtils.walkingTimeSeconds(d);
                int arrival = startSec + wsec;
                result.add(new InitialNode(s, wsec, arrival));
            }
        }
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 5) Heuristic: straight‐line walking time (seconds) from `stop` → (destLat, destLon)
    // ──────────────────────────────────────────────────────────────────────────
    private int heuristic(Stop stop, double destLat, double destLon) {
        double d = TimeAndGeoUtils.haversineMeters(
            stop.getLatitude(), stop.getLongitude(),
            destLat, destLon
        );
        return TimeAndGeoUtils.walkingTimeSeconds(d);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 6) Query the DB: get all “ride” neighbors from (stopID, tArrSec)
    //    Returns a list of intermediate “RideNeighbor” objects that store:
    //      - tripID
    //      - departureSec
    //      - arrivalSec
    //      - nextStopID
    //      - route fields so we can build a RouteInfo
    // ──────────────────────────────────────────────────────────────────────────

    private static class RideNeighbor {
        final String tripID;
        final int departureSec;
        final int arrivalSec;
        final String nextStopID;
        final String routeID;
        final String routeShortName;
        final String routeLongName;
        final String routeType;

        RideNeighbor(
            String tripID, int departureSec, int arrivalSec, String nextStopID,
            String routeID, String routeShortName, String routeLongName, String routeType
        ) {
            this.tripID = tripID;
            this.departureSec = departureSec;
            this.arrivalSec = arrivalSec;
            this.nextStopID = nextStopID;
            this.routeID = routeID;
            this.routeShortName = routeShortName;
            this.routeLongName = routeLongName;
            this.routeType = routeType;
        }
    }

    /**
     * Returns a list of RideNeighbor for every valid boarding at `stopID`
     * between tArrSec and tArrSec + MAX_WAIT_SECONDS.  Then for each boarding,
     * we query *all* downstream stops on that same trip and return them.
     */
    private List<RideNeighbor> getRideNeighbors(
        Connection conn,
        String stopID,
        int tArrSec
    ) throws SQLException {
        List<RideNeighbor> result = new ArrayList<>();

        // 6.1) Format times as "HH:MM:SS"
        String tArrStr       = TimeAndGeoUtils.secondsToTimeString(tArrSec);
        String tArrPlus1hStr = TimeAndGeoUtils.secondsToTimeString(tArrSec + MAX_WAIT_SECONDS);

        // 6.2) Step #1: find all departures from `stopID` in [tArrStr .. tArrPlus1hStr]
        String q1 = 
          "SELECT st1.trip_id, st1.departure_time AS depTime, st1.stop_sequence AS seq1, " +
          "       r.route_id, r.route_short_name, r.route_long_name, r.route_type " +
          "FROM stop_times AS st1 " +
          "JOIN trips        AS t  ON st1.trip_id = t.trip_id " +
          "JOIN routes       AS r  ON t.route_id  = r.route_id " +
          "WHERE st1.stop_id   = ? " +
          "  AND st1.departure_time >= ? " +
          "  AND st1.departure_time <= ? " +
          "ORDER BY st1.departure_time ASC";

        try (PreparedStatement ps1 = conn.prepareStatement(q1)) {
            ps1.setString(1, stopID);
            ps1.setString(2, tArrStr);
            ps1.setString(3, tArrPlus1hStr);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                String tripID        = rs1.getString("trip_id");
                String depTimeStr    = rs1.getString("depTime");
                int seq1             = rs1.getInt("seq1");
                String routeID       = rs1.getString("route_id");
                String routeShort    = rs1.getString("route_short_name");
                String routeLong     = rs1.getString("route_long_name");
                String routeType     = rs1.getString("route_type");

                int depSec = TimeAndGeoUtils.timeStringToSeconds(depTimeStr);

                // 6.3) Step #2: for each (tripID, seq1), fetch *all downstream stops*
                String q2 = 
                  "SELECT stop_id AS nextStop, arrival_time AS arrTime, stop_sequence AS seq2 " +
                  "FROM stop_times " +
                  "WHERE trip_id = ? AND stop_sequence > ? " +
                  "ORDER BY stop_sequence ASC";

                try (PreparedStatement ps2 = conn.prepareStatement(q2)) {
                    ps2.setString(1, tripID);
                    ps2.setInt(2, seq1);
                    ResultSet rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        String nextStopID   = rs2.getString("nextStop");
                        String arrTimeStr   = rs2.getString("arrTime");
                        int arrSec = TimeAndGeoUtils.timeStringToSeconds(arrTimeStr);

                        // Encode this neighbor
                        RideNeighbor rn = new RideNeighbor(
                            tripID,
                            depSec,
                            arrSec,
                            nextStopID,
                            routeID,
                            routeShort,
                            routeLong,
                            routeType
                        );
                        result.add(rn);
                    }
                }
            }
        }

        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 7) A small helper class for the PriorityQueue
    // ──────────────────────────────────────────────────────────────────────────
    private static class QueueEntry implements Comparable<QueueEntry> {
        final int fScore;
        final int tieBreak; // to avoid comparing AStarNode directly
        final AStarNode node;

        QueueEntry(int fScore, int tieBreak, AStarNode node) {
            this.fScore = fScore;
            this.tieBreak = tieBreak;
            this.node = node;
        }

        @Override
        public int compareTo(QueueEntry other) {
            if (this.fScore != other.fScore) {
                return Integer.compare(this.fScore, other.fScore);
            }
            return Integer.compare(this.tieBreak, other.tieBreak);
        }
    }
}
