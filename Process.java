import java.util.*;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

public class Process {
    static final int GESTURE_FIST = 0;
    static final int GESTURE_ONE = 1;
    static final int GESTURE_PREV = 2;
    static final int GESTURE_VOLUP = 3;
    static int stableGesture = -1;
    static int frameCount = 0;
    static int lastGesture = -1;
    static long lastExecutionTime = 0;
    static int x1 = 300, y1 = 100;
    static int x2 = 600, y2 = 400;
    public static void main(String[] args) {
        System.load(//Load OpenCV);
        VideoCapture cam = new VideoCapture(0);
        if (!cam.isOpened()) {
            System.out.println("Camera not detected");
            return;
        }
        Mat frame = new Mat();
        while (true) {
            cam.read(frame);
            Core.flip(frame, frame, 1);
            Mat roi = frame.submat(y1, y2, x1, x2);
            Imgproc.rectangle(frame, new Point(x1, y1), new Point(x2, y2),
                    new Scalar(0, 255, 0), 2);
            int gesture = detectGesture(roi);
            if (gesture == stableGesture) {
                frameCount++;
            } 
            else {
                stableGesture = gesture;
                frameCount = 0;
            }
            if (frameCount > 4 && gesture != -1 && canExecute()) {
                executeCommand(gesture);
            }
            HighGui.imshow("Camera", frame);
            int key = HighGui.waitKey(1); 
            if (key == 27 || key == 'q') break;
        }
        cam.release();
        HighGui.destroyAllWindows();
    }
    static int detectGesture(Mat roi) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(roi, hsv, Imgproc.COLOR_BGR2HSV);
        Scalar lower = new Scalar(0, 30, 60);
        Scalar upper = new Scalar(20, 150, 255);
        Mat mask = new Mat();
        Core.inRange(hsv, lower, upper, mask);
        Imgproc.GaussianBlur(mask, mask, new Size(5, 5), 0);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
        HighGui.imshow("Mask", mask);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.isEmpty()) return -1;
        double maxArea = 0;
        int maxIndex = -1;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxArea) {
                maxArea = area;
                maxIndex = i;
            }
        }
        if (maxArea < 3000) return -1;
        Imgproc.drawContours(roi, contours, maxIndex, new Scalar(0, 0, 255), 2);
        if (maxArea > 35000) return GESTURE_VOLUP;
        else if (maxArea > 22000) return GESTURE_PREV;
        else if (maxArea > 8000) return GESTURE_FIST;
        else return GESTURE_ONE;
    }
    static void executeCommand(int gesture) {
        try {
            switch (gesture) {
                    case GESTURE_ONE:
                    System.out.println("Next Slide");
                    Runtime.getRuntime().exec(
                        "powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{RIGHT}')\"");
                    break;
                case GESTURE_PREV:
                    System.out.println("Previous Slide");
                    Runtime.getRuntime().exec(
                        "powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{LEFT}')\"");
                    break;
                case GESTURE_VOLUP:
                    System.out.println("Volume Up");
                    Runtime.getRuntime().exec(
                        "powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]175)\"");
                    break;
                case GESTURE_FIST:
                    System.out.println("Mute");
                    Runtime.getRuntime().exec(
                        "powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]173)\"");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static boolean canExecute() {
        long now = System.currentTimeMillis();
        if (now - lastExecutionTime > 700) { 
            lastExecutionTime = now;
            return true;
        }
        return false;
    }
}
