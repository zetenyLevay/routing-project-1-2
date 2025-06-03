package routing.routingEngineDijkstra.badAStar;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;

public class StopAStar extends Stop {
    private final StopTypeAStar stopType;

    public StopAStar(String stopID, String stopName, Coordinates stopCoordinates, int code, String parentStationID) {
        super(stopID, stopName, stopCoordinates, code, parentStationID);
        this.stopType = StopTypeAStar.fromCode(code);
    }

    public StopTypeAStar getStopType() {
        return stopType;
    }

    public boolean isPlatform() {
        return stopType == StopTypeAStar.PLATFORM;
    }
}
