package nlc;

import java.awt.Color;

public class ColorGradient {
    public static Color getGreenToRedGradient(float normalizedValue) {
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));
        float bias = (float) Math.pow(normalizedValue, 0.3f);
        return new Color(bias, 1 - bias, 0);
    }
}
