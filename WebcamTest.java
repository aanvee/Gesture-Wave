import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

public class WebcamTest {

    static final int GESTURE_FIST = 0;
    static final int GESTURE_ONE = 1;
    static final int GESTURE_TWO = 2;
    static final int GESTURE_PALM = 5;
    static int lastGesture = -1;
    static int x1 = 300, y1 = 100;
    static int x2 = 600, y2 = 400;
    static long lastExecutionTime = 0;
    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture camera = initializeCamera();
        Mat frame = new Mat();
        while (true) {
            if (camera.read(frame)) {
                processFrame(frame); // flip the frame
                Mat roi = extractROI(frame);
                 drawROI(frame);
                int gesture = detectGesture(roi);
               if (gesture != lastGesture && canExecute()) {
                    executeCommand(gesture);
                    lastGesture = gesture;
                }   
                display(frame, roi);

                if (handleExit())
                    break;
            }
        }

        cleanup(camera);
    }

    static VideoCapture initializeCamera() {
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Camera not detected");
            System.exit(0);
        }

        System.out.println("Camera started!");
        return camera;
    }

    static void processFrame(Mat frame) {
        Core.flip(frame, frame, 1);
    }

    static Mat extractROI(Mat frame) {
        return frame.submat(y1, y2, x1, x2);
    }
    static void drawROI(Mat frame) {
        Imgproc.rectangle(
                frame,
                new Point(x1, y1),
                new Point(x2, y2),
                new Scalar(0, 255, 0),
                2);
    }

    // Display windows
    static void display(Mat frame, Mat roi) {
        HighGui.imshow("Gesture Input", frame);
        HighGui.imshow("ROI", roi);

        HighGui.moveWindow("Gesture Input", 100, 100);
        HighGui.moveWindow("ROI", 750, 100);
        HighGui.resizeWindow("ROI", 300, 300);
    }

    // Handle exit keys
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

    
    static int detectGesture(Mat roi) {

    Mat gray = new Mat();
    Imgproc.cvtColor(roi, gray, Imgproc.COLOR_BGR2GRAY);

    Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

    Mat thresh = new Mat();
    Imgproc.threshold(gray, thresh, 100, 255, Imgproc.THRESH_BINARY_INV);

    HighGui.imshow("Threshold", thresh);

    List<MatOfPoint> contours = new ArrayList<>();
    Imgproc.findContours(thresh, contours, new Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE);

    double maxArea = 0;
    int maxIndex = -1;

    for (int i = 0; i < contours.size(); i++) {
        double area = Imgproc.contourArea(contours.get(i));
        if (area > maxArea) {
            maxArea = area;
            maxIndex = i;
        }
    }

    if (maxIndex != -1) {
        Imgproc.drawContours(roi, contours, maxIndex, new Scalar(0, 0, 255), 2);
    }

    if (maxArea > 8000) {
        return 1; // hand detected
    } else {
        return 0; // no hand
    }
}


static void executeCommand(int gesture) {
    try {
        switch (gesture) {

            case GESTURE_ONE:
                System.out.println("Next Slide");
                // Simulate right arrow key
                Runtime.getRuntime().exec("powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{RIGHT}')\"");
                break;

            case GESTURE_TWO:
                System.out.println("Previous Slide");
                Runtime.getRuntime().exec("powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{LEFT}')\"");
                break;

            case GESTURE_PALM:
                System.out.println("Volume Up");
                Runtime.getRuntime().exec("powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]175)\"");
                break;

            case GESTURE_FIST:
                System.out.println("Mute");
                Runtime.getRuntime().exec("powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]173)\"");
                break;
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
static boolean canExecute() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastExecutionTime > 1000) { // 1 second gap
        lastExecutionTime = currentTime;
        return true;
    }
    return false;
}
}
