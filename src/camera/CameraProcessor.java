package camera;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class CameraProcessor implements Runnable {

    static final int MUTE = 3;
    static final int NEXT = 1;
    static final int PREV = 2;
    static final int VOLUP = 4;

    private volatile boolean running = false;
    private final CameraListener listener;
    
    private int stableGesture = -1;
    private int frameCount = 0;
    private long lastExecutionTime = 0;

    private int x1, y1, x2, y2;

    public CameraProcessor(CameraListener listener) {
        this.listener = listener;
        // Optimized path from original App.java
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java4120.dll");
    }

    public void start() {
        if (!running) {
            running = true;
            new Thread(this, "CameraProcessorThread").start();
        }
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        VideoCapture cam = new VideoCapture(0);

        if (!cam.isOpened()) {
            if (listener != null) listener.onStatusUpdate("Error: Camera not detected");
            return;
        }

        Mat frame = new Mat();
        cam.read(frame);

        if (frame.empty()) {
            if (listener != null) listener.onStatusUpdate("Error: Empty frame from camera");
            cam.release();
            return;
        }

        int width = frame.cols();
        int height = frame.rows();

        x1 = width / 4;
        x2 = (width * 3) / 4;
        y1 = height / 5;
        y2 = (height * 4) / 5;

        if (listener != null) listener.onStatusUpdate("Camera Active: " + width + "x" + height);

        while (running) {
            if (!cam.read(frame) || frame.empty()) break;

            Core.flip(frame, frame, 1);
            
            int safeX1 = Math.max(0, x1);
            int safeY1 = Math.max(0, y1);
            int safeX2 = Math.min(frame.cols(), x2);
            int safeY2 = Math.min(frame.rows(), y2);

            if (safeX2 - safeX1 > 0 && safeY2 - safeY1 > 0) {
                Mat roi = frame.submat(safeY1, safeY2, safeX1, safeX2);

                Imgproc.rectangle(frame,
                        new Point(safeX1, safeY1),
                        new Point(safeX2, safeY2),
                        new Scalar(0, 255, 0), 2);

                int gesture = detectGesture(roi);
                
                String gestureName = "NONE";
                if (gesture != -1) {
                    if (gesture == stableGesture) {
                        frameCount++;
                    } else {
                        stableGesture = gesture;
                        frameCount = 0;
                    }

                    if (frameCount > 8 && canExecute()) {
                        gestureName = executeCommand(gesture);
                    }
                } else {
                    stableGesture = -1;
                    frameCount = 0;
                }
                
                if (listener != null) {
                    listener.onGestureDetected(getGestureString(gesture), gesture);
                }
            }

            if (listener != null) {
                listener.onFrameProcessed(matToBufferedImage(frame));
            }

            try {
                Thread.sleep(30); // Approx 30 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        cam.release();
        if (listener != null) listener.onStatusUpdate("Camera Stopped");
    }

    private String getGestureString(int gesture) {
        switch (gesture) {
            case NEXT: return "NEXT (1 Finger)";
            case PREV: return "PREV (2 Fingers)";
            case MUTE: return "MUTE (3 Fingers)";
            case VOLUP: return "VOL UP (4+ Fingers)";
            default: return "NONE";
        }
    }

    private int detectGesture(Mat roi) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(roi, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lower = new Scalar(0, 20, 70);
        Scalar upper = new Scalar(20, 255, 255);

        Mat mask = new Mat();
        Core.inRange(hsv, lower, upper, mask);

        Imgproc.GaussianBlur(mask, mask, new Size(5, 5), 0);
        Imgproc.medianBlur(mask, mask, 5);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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
        Imgproc.drawContours(roi, contours, maxIndex, new Scalar(0, 255, 0), 2);

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
                    Imgproc.circle(roi, far, 5, new Scalar(255, 0, 0), -1);
                }
            }
        }

        fingerCount = Math.min(fingerCount + 1, 5);
        if (fingerCount == 3) return MUTE;
        else if (fingerCount == 1) return NEXT;
        else if (fingerCount == 2) return PREV;
        else if (fingerCount >= 4) return VOLUP;
        
        return -1;
    }

    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private String executeCommand(int gesture) {
        String result = "";
        try {
            switch (gesture) {
                case NEXT:
                    result = "Next Slide";
                    Runtime.getRuntime().exec("powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{RIGHT}')\"");
                    break;
                case PREV:
                    result = "Previous Slide";
                    Runtime.getRuntime().exec("powershell -command \"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SendKeys]::SendWait('{LEFT}')\"");
                    break;
                case VOLUP:
                    result = "Volume Up";
                    Runtime.getRuntime().exec("powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]175)\"");
                    break;
                case MUTE:
                    result = "Mute";
                    Runtime.getRuntime().exec("powershell -command \"(New-Object -ComObject WScript.Shell).SendKeys([char]173)\"");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean canExecute() {
        long now = System.currentTimeMillis();
        if (now - lastExecutionTime > 800) {
            lastExecutionTime = now;
            return true;
        }
        return false;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
}
