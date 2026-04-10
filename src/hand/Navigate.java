package hand;
import javax.swing.*;
import java.awt.*;
import java.awt.Font;


public class Navigate {
	
	
	public Navigate(String username) 
	{
	
	
	
	
		JFrame frame=new JFrame("Hand Gesture Recognition");
		frame.setSize(1920, 1080);
		frame.setLayout(new BorderLayout());
        
        JPanel panel1=new JPanel();
        /*JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel1.add(welcomeLabel);*/
        
        JLabel label = new JLabel("Gesture Wave");
        
        label.setFont(new Font("Times New Roman", Font.BOLD, 28));
        panel1.add(label);
        
        frame.add(panel1, BorderLayout.NORTH);
        
        JPanel panel2=new JPanel(new GridLayout(1, 3));
        
        JPanel cameraPanel = new JPanel();
        JPanel gesturePanel = new JPanel();
        JPanel controlPanel = new JPanel();
        
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera Feed"));
        cameraPanel.setBackground(Color.LIGHT_GRAY);

        gesturePanel.setBorder(BorderFactory.createTitledBorder("Gesture Info"));
        gesturePanel.setBackground(Color.WHITE);

        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        controlPanel.setBackground(Color.CYAN);
        
        cameraPanel.add(new JLabel("Camera Placeholder"));
        gesturePanel.add(new JLabel("Gesture: NONE"));
        gesturePanel.add(new JLabel("Status:IDLE"));
        gesturePanel.add(new JLabel("Cooldown: Ready"));
        gesturePanel.add(new JLabel("Confidence: 0%"));
        JButton startBtn = new JButton("Start Camera");
        JButton stopBtn = new JButton("Stop Camera");
        JButton resetBtn = new JButton("Reset");

        startBtn.addActionListener(e -> {
            try {
            	new ProcessBuilder("java", "-cp", ".;lib/opencv-4120.jar", "camera.App").start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        stopBtn.addActionListener(e -> {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM java.exe");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        resetBtn.addActionListener(e -> {
            System.out.println("Reset clicked");
        });

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(resetBtn);
        
        panel2.add(cameraPanel);
        panel2.add(gesturePanel);
        panel2.add(controlPanel);
        
        frame.add(panel2, BorderLayout.CENTER);
        
        
        JPanel bottomContainer=new JPanel(new GridLayout(1, 2));
        
        JPanel gestureMapping = new JPanel();
        
        gestureMapping.setBorder(BorderFactory.createTitledBorder("Gesture->Action Mapping"));
        gestureMapping.setBackground(Color.LIGHT_GRAY);
        
        JPanel logsPanel = new JPanel();
        logsPanel.setBorder(BorderFactory.createTitledBorder("Logs / Status"));
        logsPanel.setBackground(Color.WHITE);
        JTextArea logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        logsPanel.add(new JScrollPane(logArea));
        
        bottomContainer.add(gestureMapping);
        bottomContainer.add(logsPanel);
        
        frame.add(bottomContainer, BorderLayout.SOUTH);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        frame.setVisible(true);
        
	}
	
	

}