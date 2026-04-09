package auth;

import javax.swing.SwingUtilities;
public class MainApp {
    public static void main(String[] args) {
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info
                    : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {  }

        SwingUtilities.invokeLater(() -> {
            new AuthFrame().setVisible(true);
        });
    }
}