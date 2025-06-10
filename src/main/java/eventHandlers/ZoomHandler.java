package eventHandlers;

import org.jxmapviewer.JXMapViewer;

public class ZoomHandler {

    private JXMapViewer mapViewer;

    /**
     * Constructor for ZoomHandler.
     *
     * @param mapViewer The JXMapViewer instance to control zooming.
     */
    public ZoomHandler(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    /**
     * Zooms in on the map by decreasing the zoom level.
     * The zoom level is typically an integer where a lower number means a closer zoom.
     */
    public void zoomIn() {
        mapViewer.setZoom(mapViewer.getZoom() - 1);
    }

    /**
     * Zooms out on the map by increasing the zoom level.
     * The zoom level is typically an integer where a higher number means a closer zoom.
     */
    public void zoomOut() {
        mapViewer.setZoom(mapViewer.getZoom() + 1);
    }
}
