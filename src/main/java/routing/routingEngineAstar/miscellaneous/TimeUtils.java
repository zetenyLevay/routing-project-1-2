package routing.routingEngineAstar.miscellaneous;

public class TimeUtils {
    

      /**
     * Converts a time string "HH:mm:ss" to total seconds since midnight.
     */
    public static int timeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        int s = Integer.parseInt(parts[2]);
        return h * 3600 + m * 60 + s;
    }

        /**
     * Converts total seconds since midnight back to "HH:mm:ss".
     */
    public static String secondsToTime(int totalSeconds) {
        int h = totalSeconds / 3600;
        int rem = totalSeconds % 3600;
        int m = rem / 60;
        int s = rem % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
