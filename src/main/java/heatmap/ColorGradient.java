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

}