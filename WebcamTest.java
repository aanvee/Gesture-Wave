import org.opencv.core.*;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class WebcamTest {

    // ROI coordinates (global for reuse)
    static int x1 = 300, y1 = 100;
    static int x2 = 600, y2 = 400;

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = initializeCamera();

        Mat frame = new Mat();

        while (true) {
            if (camera.read(frame)) {

                processFrame(frame); //flip the frame

                Mat roi = extractROI(frame);

                drawROI(frame);

                display(frame, roi);

                if (handleExit()) break;
            }
        }

        cleanup(camera);
    }

    //  Initialize camera
    static VideoCapture initializeCamera() {
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Camera not detected");
            System.exit(0);
        }

        System.out.println("Camera started!");
        return camera;
    }

    //  Flip frame
    static void processFrame(Mat frame) {
        Core.flip(frame, frame, 1);
    }

    //  Extract ROI
    static Mat extractROI(Mat frame) {
        return frame.submat(y1, y2, x1, x2);
    }

    //  Draw ROI box
    static void drawROI(Mat frame) {
        Imgproc.rectangle(
                frame,
                new Point(x1, y1),
                new Point(x2, y2),
                new Scalar(0, 255, 0),
                2
        );
    }

    //  Display windows
    static void display(Mat frame, Mat roi) {
        HighGui.imshow("Gesture Input", frame);
        HighGui.imshow("ROI", roi);

        HighGui.moveWindow("Gesture Input", 100, 100);
        HighGui.moveWindow("ROI", 750, 100);
        HighGui.resizeWindow("ROI", 300, 300);
    }

    //  Handle exit keys
    static boolean handleExit() {
        int key = HighGui.waitKey(10);
        return (key == 'q' || key == 27);
    }

    // Cleanup resources
    static void cleanup(VideoCapture camera) {
        System.out.println("Exiting program...");
        camera.release();
        HighGui.destroyAllWindows();
        System.exit(0);
    }
}
