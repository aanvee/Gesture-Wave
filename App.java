import java.util.*;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

public class App {

    static final int MUTE = 3;
    static final int NEXT = 1;
    static final int PREV = 2;
    static final int VOLUP = 4;

    static int stableGesture = -1;
    static int frameCount = 0;
    static long lastExecutionTime = 0;

    static int x1, y1, x2, y2;

    public static void main(String[] args) {

        System.load("C:\\opencv\\build\\java\\x64\\opencv_java4120.dll");

        VideoCapture cam = new VideoCapture(0);

        if (!cam.isOpened()) {
            System.out.println("Camera not detected");
            return;
        }

        Mat frame = new Mat();
        cam.read(frame);

        int width = frame.cols();
        int height = frame.rows();

        System.out.println("Resolution: " + width + " x " + height);

        x1 = width / 4;
        x2 = (width * 3) / 4;
        y1 = height / 5;
        y2 = (height * 4) / 5;

        while (true) {
            cam.read(frame);
            if (frame.empty()) break;
            Core.flip(frame, frame, 1);
            int safeX1 = Math.max(0, x1);
            int safeY1 = Math.max(0, y1);
            int safeX2 = Math.min(frame.cols(), x2);
            int safeY2 = Math.min(frame.rows(), y2);

            if (safeX2 - safeX1 <= 0 || safeY2 - safeY1 <= 0) {
                HighGui.imshow("Camera", frame);
                continue;
            }
            Mat roi = frame.submat(safeY1, safeY2, safeX1, safeX2);

            Imgproc.rectangle(frame,
                    new Point(safeX1, safeY1),
                    new Point(safeX2, safeY2),
                    new Scalar(0, 255, 0), 2);

            int gesture = detectGesture(roi);
            if (gesture == stableGesture) {
                frameCount++;
            } else {
                stableGesture = gesture;
                frameCount = 0;
            }

            if (frameCount > 8 && gesture != -1 && canExecute()) {
                executeCommand(gesture);
            }

            HighGui.imshow("Camera", frame);

            if (HighGui.waitKey(1) == 27) break;
        }

        cam.release();
        HighGui.destroyAllWindows();
    }

    static int detectGesture(Mat roi) {

        Mat hsv = new Mat();
        Imgproc.cvtColor(roi, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lower = new Scalar(0, 20, 70);
        Scalar upper = new Scalar(20, 255, 255);

        Mat mask = new Mat();
        Core.inRange(hsv, lower, upper, mask);

        Imgproc.GaussianBlur(mask, mask, new Size(5, 5), 0);
        Imgproc.medianBlur(mask, mask, 5);

        Mat kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_ELLIPSE, new Size(5, 5));

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

        if (maxArea < 5000) return -1;

        MatOfPoint handContour = contours.get(maxIndex);

        Imgproc.drawContours(roi, contours, maxIndex,
                new Scalar(0, 255, 0), 2);

        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(handContour, hull);

        MatOfInt4 defects = new MatOfInt4();

        if (hull.toArray().length > 3)
            Imgproc.convexityDefects(handContour, hull, defects);

        int fingerCount = 0;

        if (defects.rows() > 0) {

            List<Integer> defectList = defects.toList();
            Point[] contourArray = handContour.toArray();

            for (int i = 0; i < defectList.size(); i += 4) {

                Point start = contourArray[defectList.get(i)];
                Point end = contourArray[defectList.get(i + 1)];
                Point far = contourArray[defectList.get(i + 2)];

                double a = distance(start, end);
                double b = distance(start, far);
                double c = distance(end, far);

                double angle = Math.acos((b*b + c*c - a*a) / (2*b*c));

                if (angle < Math.PI / 2) {
                    fingerCount++;
                    Imgproc.circle(roi, far, 5,
                            new Scalar(255, 0, 0), -1);
                }
            }
        }

        fingerCount = Math.min(fingerCount + 1, 5);

        if (fingerCount == 3) return MUTE;
        else if (fingerCount == 1) return NEXT;
        else if (fingerCount == 2) return PREV;
        else return VOLUP;
    }

    static double distance(Point p1, Point p2) {
        return Math.sqrt(
                Math.pow(p1.x - p2.x, 2) +
                Math.pow(p1.y - p2.y, 2)
        );
    }
 
    static void executeCommand(int gesture) {

        try {
            switch (gesture) {

                case NEXT:
                    System.out.println("Next Slide");
                    Runtime.getRuntime().exec(
                            "powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{RIGHT}')\"");
                    break;

                case PREV:
                    System.out.println("Previous Slide");
                    Runtime.getRuntime().exec(
                            "powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{LEFT}')\"");
                    break;

                case VOLUP:
                    System.out.println("Volume Up");
                    Runtime.getRuntime().exec(
                            "powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]175)\"");
                    break;

                case MUTE:
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

        if (now - lastExecutionTime > 800) {
            lastExecutionTime = now;
            return true;
        }
        return false;
    }
}
