package closureAnalysis.builders;

import closureAnalysis.data.enums.TransportType;
import closureAnalysis.data.models.NearbyPOIs;

/**
 * StopBuilder interface defines the methods required to build a stop object.
 * It allows setting various properties of a stop such as transport type, route amount,
 * passenger count, stop worth, and nearby points of interest (POIs).
 */
public interface StopBuilder {
    public void reset();
    public void setTransportType(TransportType type);
    public void setRouteAmount(int amount);
    public void setPassengerCount(int count);
    public void setStopWorth(int stopWorth);
    public void setNearbyPOIs(NearbyPOIs nearbyPOIs);
}
