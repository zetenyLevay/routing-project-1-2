// package gui;

// import javax.imageio.ImageIO;
// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.MouseAdapter;
// import java.awt.event.MouseEvent;
// import java.awt.geom.AffineTransform;
// import java.awt.geom.Ellipse2D;
// import java.awt.geom.Point2D;
// import java.awt.image.BufferedImage;
// import java.io.*;
// import java.util.Arrays;
// import java.util.List;

// public class OfflineMapPanel extends JPanel {
//     private final BufferedImage baseMapImage;
//     private final BufferedImage heatmapOverlayBase;
//     private final BufferedImage stopsOverlayBase;

//     private BufferedImage scaledBaseMapImage;
//     private BufferedImage scaledHeatmapOverlayImage;
//     private BufferedImage scaledStopsOverlayImage;

//     private final double minimumLatitude;
//     private final double maximumLatitude;
//     private final double minimumLongitude;
//     private final double maximumLongitude;
//     private final List<GeoPosition> stopCoordinates;

//     private double zoomFactor;
//     private double offsetX;
//     private double offsetY;
//     private Point dragStartPoint;
//     private boolean showHeatmap = true;

//     public OfflineMapPanel(
//             double minLat,
//             double maxLat,
//             double minLon,
//             double maxLon,
//             List<GeoPosition> stops
//     ) throws IOException {
//         this.minimumLatitude = minLat;
//         this.maximumLatitude = maxLat;
//         this.minimumLongitude = minLon;
//         this.maximumLongitude = maxLon;
//         this.stopCoordinates = stops;

//         this.baseMapImage = loadMapImage();
//         this.heatmapOverlayBase = generateHeatmapOverlayBase();
//         this.stopsOverlayBase = generateStopsOverlayBase();

//         setupInitialView();
//         updateScaledImages();
//         setupMouseInteractions();
//     }

//     public void setShowHeatmap(boolean shouldDisplay) {
//         this.showHeatmap = shouldDisplay;
//         repaint();
//     }

//     private BufferedImage loadMapImage() throws IOException {
//         try (InputStream inputStream = getClass().getResourceAsStream("/mapImage.jpg")) {
//             if (inputStream == null) throw new FileNotFoundException("mapImage.jpg not found");
//             return ImageIO.read(inputStream);
//         }
//     }

//     private void setupInitialView() {
//         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//         int maxWidth = screenSize.width - 50;
//         int maxHeight = screenSize.height - 50;

//         double scaleWidth = (double) maxWidth / baseMapImage.getWidth();
//         double scaleHeight = (double) maxHeight / baseMapImage.getHeight();
//         this.zoomFactor = Math.min(1.0, Math.min(scaleWidth, scaleHeight));

//         int preferredWidth = (int) (baseMapImage.getWidth() * zoomFactor);
//         int preferredHeight = (int) (baseMapImage.getHeight() * zoomFactor);
//         setPreferredSize(new Dimension(preferredWidth, preferredHeight));

//         this.offsetX = 0;
//         this.offsetY = 0;
//     }

//     private void setupMouseInteractions() {
//         addMouseWheelListener(event -> {
//             double previousZoom = zoomFactor;
//             zoomFactor *= (event.getWheelRotation() < 0 ? 1.1 : 0.9);
//             zoomFactor = Math.max(previousZoom / 4, Math.min(previousZoom * 4, zoomFactor));

//             double mouseX = event.getX();
//             double mouseY = event.getY();
//             offsetX = mouseX - (mouseX - offsetX) * (zoomFactor / previousZoom);
//             offsetY = mouseY - (mouseY - offsetY) * (zoomFactor / previousZoom);

//             updateScaledImages();
//             repaint();
//         });

//         MouseAdapter dragAdapter = new MouseAdapter() {
//             public void mousePressed(MouseEvent event) {
//                 dragStartPoint = event.getPoint();
//             }

//             public void mouseDragged(MouseEvent event) {
//                 Point currentPoint = event.getPoint();
//                 offsetX += currentPoint.x - dragStartPoint.x;
//                 offsetY += currentPoint.y - dragStartPoint.y;
//                 dragStartPoint = currentPoint;
//                 repaint();
//             }
//         };

//         addMouseListener(dragAdapter);
//         addMouseMotionListener(dragAdapter);
//     }

//     private void updateScaledImages() {
//         int newWidth = (int) (baseMapImage.getWidth() * zoomFactor);
//         int newHeight = (int) (baseMapImage.getHeight() * zoomFactor);

//         scaledBaseMapImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D gBase = scaledBaseMapImage.createGraphics();
//         gBase.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//         gBase.drawImage(baseMapImage, 0, 0, newWidth, newHeight, null);
//         gBase.dispose();

//         scaledHeatmapOverlayImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D gHeat = scaledHeatmapOverlayImage.createGraphics();
//         gHeat.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//         gHeat.drawImage(heatmapOverlayBase, 0, 0, newWidth, newHeight, null);
//         gHeat.dispose();

//         scaledStopsOverlayImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D gStops = scaledStopsOverlayImage.createGraphics();
//         gStops.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//         gStops.drawImage(stopsOverlayBase, 0, 0, newWidth, newHeight, null);
//         gStops.dispose();
//     }

//     private BufferedImage generateHeatmapOverlayBase() {
//         int width = baseMapImage.getWidth();
//         int height = baseMapImage.getHeight();
//         int[][] heatValues = new int[width][height];
//         double influenceRadius = 120.0;
//         int radiusPixels = (int) Math.ceil(influenceRadius);

//         for (GeoPosition position : stopCoordinates) {
//             Point2D centerPixel = convertGeoToPixel(position);
//             int centerX = (int) Math.round(centerPixel.getX());
//             int centerY = (int) Math.round(centerPixel.getY());

//             for (int dy = -radiusPixels; dy <= radiusPixels; dy++) {
//                 for (int dx = -radiusPixels; dx <= radiusPixels; dx++) {
//                     int x = centerX + dx;
//                     int y = centerY + dy;
//                     if (x < 0 || x >= width || y < 0 || y >= height) continue;
//                     double distanceSquared = (dx + 0.5) * (dx + 0.5) + (dy + 0.5) * (dy + 0.5);
//                     if (distanceSquared <= influenceRadius * influenceRadius) {
//                         heatValues[x][y]++;
//                     }
//                 }
//             }
//         }

//         int maxHeat = Arrays.stream(heatValues).flatMapToInt(Arrays::stream).max().orElse(0);
//         Color[] colorRamp = new Color[maxHeat + 1];
//         for (int i = 1; i <= maxHeat; i++) {
//             float hue = 1.0f - Math.min(i, 10) / 10.0f;
//             colorRamp[i] = Color.getHSBColor(hue, 1.0f, 1.0f);
//         }

//         BufferedImage heatmap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//         for (int y = 0; y < height; y++) {
//             for (int x = 0; x < width; x++) {
//                 int value = heatValues[x][y];
//                 if (value <= 1) continue;
//                 Color color = colorRamp[Math.min(value, maxHeat)];
//                 int rgba = (color.getRGB() & 0x00FFFFFF) | (180 << 24);
//                 heatmap.setRGB(x, y, rgba);
//             }
//         }
//         return heatmap;
//     }

//     private BufferedImage generateStopsOverlayBase() {
//         int width = baseMapImage.getWidth();
//         int height = baseMapImage.getHeight();
//         BufferedImage overlay = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//         Graphics2D g2d = overlay.createGraphics();
//         g2d.setColor(Color.RED);
//         for (GeoPosition position : stopCoordinates) {
//             Point2D pixel = convertGeoToPixel(position);
//             double x = pixel.getX() - 2;
//             double y = pixel.getY() - 2;
//             g2d.fill(new Ellipse2D.Double(x, y, 4, 4));
//         }
//         g2d.dispose();
//         return overlay;
//     }

//     @Override
//     protected void paintComponent(Graphics graphics) {
//         super.paintComponent(graphics);
//         Graphics2D g2d = (Graphics2D) graphics;
//         g2d.drawImage(scaledBaseMapImage, (int) offsetX, (int) offsetY, null);
//         if (showHeatmap) {
//             g2d.drawImage(scaledHeatmapOverlayImage, (int) offsetX, (int) offsetY, null);
//         }
//         g2d.drawImage(scaledStopsOverlayImage, (int) offsetX, (int) offsetY, null);
//     }

//     private Point2D convertGeoToPixel(GeoPosition geoPosition) {
//         double xRatio = (geoPosition.getLongitude() - minimumLongitude) / (maximumLongitude - minimumLongitude);
//         double yRatio = (maximumLatitude - geoPosition.getLatitude()) / (maximumLatitude - minimumLatitude);
//         return new Point2D.Double(
//             xRatio * baseMapImage.getWidth(),
//             yRatio * baseMapImage.getHeight()
//         );
//     }
// }

