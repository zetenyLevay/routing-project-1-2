package heatmap;

import java.awt.Color;

public class ColorGradient {
    public static Color getGreenToRedGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        return new Color(normalizedValue, 1 - normalizedValue, 0);
    }

    public static Color getMultiHueGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        if (normalizedValue < 0.5f) {
            return new Color(2 * normalizedValue, 1.0f, 0);
        } else {
            return new Color(1.0f, 2 - 2 * normalizedValue, 0);
        }
    }
}