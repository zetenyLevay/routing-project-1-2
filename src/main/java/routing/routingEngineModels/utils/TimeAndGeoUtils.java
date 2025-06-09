package routing.routingEngineModels.utils;

public class TimeAndGeoUtils {
    // ────────────────────────────────────────────────────────────────────────
    // 1) Convert "HH:MM:SS" to seconds since midnight
    // ────────────────────────────────────────────────────────────────────────
    public static int timeStringToSeconds(String hhmmss) {
        // Assumes valid input "HH:MM:SS", 00 ≤ HH < 24
        String[] parts = hhmmss.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 2) Convert seconds since midnight → "HH:MM:SS"
    // ────────────────────────────────────────────────────────────────────────
    public static String secondsToTimeString(int sec) {
        // Wrap-around at 24h
        int sMod = ((sec % 86400) + 86400) % 86400;
        int h = sMod / 3600;
        int m = (sMod % 3600) / 60;
        int s = sMod % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // ────────────────────────────────────────────────────────────────────────
    // 3) Haversine distance (meters) between two lat/lon points
    // ────────────────────────────────────────────────────────────────────────
    public static double haversineMeters(
        double lat1, double lon1,
        double lat2, double lon2
    ) {
        final double R = 6_371_000.0; // Earth radius in meters
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double latdiffRad = Math.toRadians(lat2 - lat1);
        double longDiffRad = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latdiffRad/2) * Math.sin(latdiffRad/2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(longDiffRad/2) * Math.sin(longDiffRad/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ────────────────────────────────────────────────────────────────────────
    // 4) Convert a straight-line distance (meters) into walking time (seconds)
    //    assuming 5 km/h = 5000 m / 3600 s ≈ 1.3889 m/s
    // ────────────────────────────────────────────────────────────────────────
    private static final double WALK_SPEED_MPS = 5000.0 / 3600.0; // ≈1.3889 m/s
    public static int walkingTimeSeconds(double distanceMeters) {
        return (int) Math.ceil(distanceMeters / WALK_SPEED_MPS);
    }
}
