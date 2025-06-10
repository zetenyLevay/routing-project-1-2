package routing.routingEngineModels.utils;

/**
 * TimeAndGeoUtils.java
 *
 * Utility class for time and geographical calculations. Provides methods to
 * convert time strings to seconds, seconds to time strings, calculate Haversine
 * distance between two geographical points, and estimate walking time based on
 * distance.
 */
public class TimeAndGeoUtils {

    private static final double WALK_SPEED_MPS = 5000.0 / 3600.0;

    /**
     * Convert a time string in "HH:MM:SS" format to seconds since
     *
     * @param hhmmss time string in "HH:MM:SS" format
     * @return the total number of seconds since midnight
     */
    public static int timeStringToSeconds(String hhmmss) {
        String[] parts = hhmmss.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }

    /**
     * Convert seconds since midnight to a time string in "HH:MM:SS" format.
     * Wraps around at 24 hours.
     *
     * @param sec the number of seconds since midnight
     * @return the time string in "HH:MM:SS" format
     */
    public static String secondsToTimeString(int sec) {
        // Wrap-around at 24h
        int sMod = ((sec % 86400) + 86400) % 86400;
        int h = sMod / 3600;
        int m = (sMod % 3600) / 60;
        int s = sMod % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Calculate the Haversine distance between two geographical points
     *
     * @param lat1 latitude of the first point
     * @param lon1 longitude of the first point
     * @param lat2 latitude of the second point
     * @param lon2 longitude of the second point
     * @return the Haversine distance in meters
     */
    public static double haversineMeters(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        final double R = 6_371_000.0; // Earth radius in meters
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double latdiffRad = Math.toRadians(lat2 - lat1);
        double longDiffRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latdiffRad / 2) * Math.sin(latdiffRad / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(longDiffRad / 2) * Math.sin(longDiffRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Estimate walking time in seconds based on distance in meters. Assumes an
     * average walking speed of 5 km/h.
     *
     * @param distanceMeters the distance in meters
     * @return the estimated walking time in seconds
     */
    public static int walkingTimeSeconds(double distanceMeters) {
        return (int) Math.ceil(distanceMeters / WALK_SPEED_MPS);
    }
}
