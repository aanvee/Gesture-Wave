package hand;

import camera.CameraListener;
import camera.CameraProcessor;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Navigate implements CameraListener {

    private final JFrame frame;
    private final JLabel cameraView;
    private final JLabel gestureLabel;
    private final JLabel statusLabel;
    private final JTextArea logArea;
    private CameraProcessor processor;

    public Navigate(String username) {
        frame = new JFrame("Hand Gesture Recognition");
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Gesture Wave - Welcome, " + username);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 28));
        headerPanel.add(titleLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new GridLayout(1, 3));

        // 1. Camera Feed
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera Feed"));
        cameraPanel.setBackground(Color.BLACK);
        cameraView = new JLabel("Camera Ready", SwingConstants.CENTER);
        cameraView.setForeground(Color.WHITE);
        cameraPanel.add(cameraView, BorderLayout.CENTER);

        // 2. Gesture Info
        JPanel gesturePanel = new JPanel();
        gesturePanel.setLayout(new BoxLayout(gesturePanel, BoxLayout.Y_AXIS));
        gesturePanel.setBorder(BorderFactory.createTitledBorder("Gesture Info"));
        gesturePanel.setBackground(Color.WHITE);
        
        gestureLabel = new JLabel("Gesture: NONE");
        statusLabel = new JLabel("Status: IDLE");
        JLabel cooldownLabel = new JLabel("Cooldown: Ready");
        JLabel confidenceLabel = new JLabel("Confidence: 0%");
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        gestureLabel.setFont(labelFont);
        statusLabel.setFont(labelFont);
        
        gesturePanel.add(Box.createVerticalStrut(20));
        gesturePanel.add(gestureLabel);
        gesturePanel.add(Box.createVerticalStrut(10));
        gesturePanel.add(statusLabel);
        gesturePanel.add(Box.createVerticalStrut(10));
        gesturePanel.add(cooldownLabel);
        gesturePanel.add(Box.createVerticalStrut(10));
        gesturePanel.add(confidenceLabel);

        // Footer / Logs
        JPanel bottomContainer = new JPanel(new GridLayout(1, 2));

        JPanel gestureMapping = new JPanel();
        gestureMapping.setBorder(BorderFactory.createTitledBorder("Gesture Mapping"));
        gestureMapping.add(new JLabel("<html>1 Finger: NEXT<br>2 Fingers: PREV<br>3 Fingers: MUTE<br>4+ Fingers: VOL UP</html>"));

        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.setBorder(BorderFactory.createTitledBorder("Logs / Status"));
        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logsPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setBackground(new Color(240, 240, 240));

        JButton startBtn = new JButton("Start Camera");
        JButton stopBtn = new JButton("Stop Camera");
        JButton resetBtn = new JButton("Reset");

        startBtn.addActionListener(e -> {
            if (processor == null) {
                processor = new CameraProcessor(this);
                processor.start();
                onStatusUpdate("Starting camera system...");
            }
        });

        stopBtn.addActionListener(e -> {
            if (processor != null) {
                processor.stop();
                processor = null;
                cameraView.setIcon(null);
                cameraView.setText("Camera Stopped");
                onStatusUpdate("System stopped.");
            }
        });

        resetBtn.addActionListener(e -> {
            logArea.setText("");
            onStatusUpdate("Logs cleared.");
        });

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(resetBtn);

        mainPanel.add(cameraPanel);
        mainPanel.add(gesturePanel);
        mainPanel.add(controlPanel);
        frame.add(mainPanel, BorderLayout.CENTER);

        bottomContainer.add(gestureMapping);
        bottomContainer.add(logsPanel);
        frame.add(bottomContainer, BorderLayout.SOUTH);


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void onFrameProcessed(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            cameraView.setText("");
            cameraView.setIcon(new ImageIcon(image));
        });
    }

    @Override
    public void onGestureDetected(String gesture, int fingerCount) {
        SwingUtilities.invokeLater(() -> {
            gestureLabel.setText("Gesture: " + gesture);
            if (!gesture.equals("NONE")) {
                logArea.append("Detected: " + gesture + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    @Override
    public void onStatusUpdate(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Status: " + message);
            logArea.append("SYSTEM: " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}