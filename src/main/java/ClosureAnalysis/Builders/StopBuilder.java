package ClosureAnalysis.Builders;

import ClosureAnalysis.Data.Enums.TransportType;
import ClosureAnalysis.Data.Models.NearbyPOIs;

public interface StopBuilder {
    public void reset();
    public void setTransportType(TransportType type);
    public void setRouteAmount(int amount);
    public void setPassengerCount(int count);
    public void setStopWorth(int stopWorth);
    public void setNearbyPOIs(NearbyPOIs nearbyPOIs);
}
