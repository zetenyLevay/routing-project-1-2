package eventHandlers;

import org.jxmapviewer.JXMapViewer;

public class ZoomHandler {

    private JXMapViewer mapViewer;

    public ZoomHandler(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    public void zoomIn() {
        mapViewer.setZoom(mapViewer.getZoom() - 1);
    }

    public void zoomOut() {
        mapViewer.setZoom(mapViewer.getZoom() + 1);
    }
}
