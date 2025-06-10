package heatmap;

import java.awt.Color;

/**
 * Provides methods to generate color gradients for heatmap visualization based on normalized values.
 */
public class ColorGradient {

    /**
     * Generates a color gradient from green to red based on a normalized value.
     *
     * @param normalizedValue a value between 0 and 1 representing the intensity
     * @return a Color object ranging from green (0) to red (1)
     */
    public static Color getGreenToRedGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        float bias = (float) Math.pow(normalizedValue, 0.3f);
        return new Color(bias, 1 - bias, 0);
    }

    /**
     * Generates a multi-hue color gradient based on a normalized value, transitioning from green to yellow to red.
     *
     * @param normalizedValue a value between 0 and 1 representing the intensity
     * @return a Color object ranging from green (0) through yellow (0.5) to red (1)
     */
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