package camera;

import java.awt.image.BufferedImage;

/**
 * Interface for receiving updates from the CameraProcessor.
 */
public interface CameraListener {
    /**
     * Called when a new frame is processed and ready for display.
     * @param image The processed image.
     */
    void onFrameProcessed(BufferedImage image);

    /**
     * Called when a gesture is detected.
     * @param gesture The name of the gesture (e.g., "NEXT", "PREV").
     * @param fingerCount The number of fingers detected.
     */
    void onGestureDetected(String gesture, int fingerCount);

    /**
     * Called when an informative or error message occurs.
     * @param message The status message.
     */
    void onStatusUpdate(String message);
}
