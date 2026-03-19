import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class WebcamTest {
    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Camera not detected");
            return;
        }

        System.out.println("Camera started!");

        Mat frame = new Mat();

        while (true) {
            if (camera.read(frame)) {

                // Draw ROI box
                    Imgproc.rectangle(
                        frame,
                        new Point(300, 100),
                        new Point(600, 400),
                        new Scalar(0, 255, 0),
                        2
                    );
                HighGui.imshow("Gesture Input", frame);
                int key = HighGui.waitKey(10); // Giving 10ms delay  for key detection 
                if (key == 'q' || key==27) // 27 is for esc key
                {
                    break;
                }
            }
        }
        System.out.println("I am outside of the infinite loop !");
        camera.release();
        HighGui.destroyAllWindows();
        System.exit(0); // force closing the window .
    }
}