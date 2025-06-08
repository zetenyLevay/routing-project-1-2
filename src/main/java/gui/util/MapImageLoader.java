package gui.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MapImageLoader {
    public static BufferedImage load(String resourcePath) throws IOException {
        try (InputStream in = MapImageLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Map image file not found: " + resourcePath);
            }
            return ImageIO.read(in);
        }
    }
}
