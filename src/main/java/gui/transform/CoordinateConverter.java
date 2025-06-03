package gui.transform;

public class CoordinateConverter {
    public static double degreesMinutesSecondsToDecimal(int degrees, int minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }
}