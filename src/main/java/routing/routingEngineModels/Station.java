package routing.routingEngineModels;

import routing.routingEngineModels.Stop.Stop;

import java.util.List;

public class Station {
    private final List<Stop> childStops;


    public Station(List<Stop> childStops) {
        this.childStops = childStops;
    }

    public List<Stop> getChildStops() {
        return childStops;
    }
}
