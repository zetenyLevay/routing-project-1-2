package routing.routingEngineCSA.engine.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeConverter {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

    public static LocalTime parseGTFSTime(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return LocalTime.of(hours % 24, minutes, seconds);
    }

    public static String formatAsGTFSTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static int compare(LocalTime t1, LocalTime t2) {
        return t1.compareTo(t2);
    }

    public static boolean isBeforeOrEqual(LocalTime t1, LocalTime t2) {
        return t1.compareTo(t2) <= 0;
    }
}