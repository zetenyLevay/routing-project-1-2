package heatmap;

import java.awt.Color;

public class ColorGradient {
    public static Color getGreenToRedGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        float bias = (float) Math.pow(normalizedValue, 0.3f);
        return new Color(bias, 1 - bias, 0);
    }

    public static Color getMultiHueGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        float bias = (float) Math.pow(normalizedValue, 0.3f);
        if (bias < 0.5f) {
            return new Color(2 * bias, 1.0f, 0);
        } else {
            return new Color(1.0f, 2 - 2 * bias, 0);
        }
    }
}