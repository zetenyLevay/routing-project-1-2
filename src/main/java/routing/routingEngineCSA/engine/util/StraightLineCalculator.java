package routing.routingEngineCSA.engine.util;

import routing.routingEngineModels.Coordinates;
import routing.routingEngineModels.Stop.Stop;

import java.time.LocalTime;

public class StraightLineCalculator {

    private static final int AVERAGE_SPEED_MMS = 1389;
    private static final int EARTH_RADIUS_M = 6_371_000;
    private static final int SECONDS_PER_HOUR = 3600;

    public static int haversineDistanceMeters(Stop stop, Coordinates coordinates) {
        double lat1 = Math.toRadians(stop.getLatitude());
        double lat2 = Math.toRadians(coordinates.getLatitude());
        double deltaLat = Math.toRadians(coordinates.getLatitude() - stop.getLatitude());
        double deltaLon = Math.toRadians(coordinates.getLongitude() - stop.getLongitude());

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return (int) (EARTH_RADIUS_M * c);
    }

    public static int calculateWalkingTimeSeconds(Stop stop, Coordinates coordinates) {
        int distanceMeters = haversineDistanceMeters(stop, coordinates);
        return (distanceMeters * 1000) / AVERAGE_SPEED_MMS;
    }


    public static int calculateArrivalTimeSec(int departureTimeSec, Stop stop, Coordinates coordinates) {
        int walkTimeSec = calculateWalkingTimeSeconds(stop, coordinates);
        return departureTimeSec + walkTimeSec;
    }


    public static LocalTime calculateArrivalTime(LocalTime departureTime, Stop stop, Coordinates coordinates) {
        int departureSec = departureTime.toSecondOfDay();
        int arrivalSec = calculateArrivalTimeSec(departureSec, stop, coordinates);
        return LocalTime.ofSecondOfDay(arrivalSec % 86400);
    }
}