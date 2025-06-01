package routing.routingEngineModels;

public class RouteInfo {

    private String operatorNameString; //is agency_name from agency.txt in the active GTFS dataset
    private String shortNameString; //is route_short_name from routes.txt
    private String longNameString; //is route_long_name from routes.txt
    private String headSignString;//is trip_headsign from trips.txt

    public RouteInfo(String operatorNameString, String shortNameString, String longNameString, String headSignString) {
        this.operatorNameString = operatorNameString;
        this.shortNameString = shortNameString;
        this.longNameString = longNameString;
        this.headSignString = headSignString;
    }
}
