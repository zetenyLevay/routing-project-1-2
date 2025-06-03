package routing.routingEngineDijkstra.baddijkstra;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;

public class StopDijkstra extends Stop {
    private final StopTypeDijkstra stopType;

    public StopDijkstra(String stopID, String stopName, Coordinates stopCoordinates, int code, String parentStationID) {
        super(stopID, stopName, stopCoordinates, code, parentStationID);
        this.stopType = StopTypeDijkstra.fromCode(code);
    }

    public StopTypeDijkstra getStopType() {
        return stopType;
    }

    public boolean isPlatform() {
        return stopType == StopTypeDijkstra.PLATFORM;
    }
}