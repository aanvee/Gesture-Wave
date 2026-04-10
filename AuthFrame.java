package auth;


import hand.Navigate;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;


public class AuthFrame extends JFrame {

    // ── Colour palette ──────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(13,  17,  23);
    private static final Color BG_CARD      = new Color(22,  27,  34);
    private static final Color BG_INPUT     = new Color(33,  38,  45);
    private static final Color ACCENT_GREEN = new Color(35, 197, 122);
    private static final Color ACCENT_BLUE  = new Color(56, 139, 253);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_MUTED   = new Color(139, 148, 158);
    private static final Color BORDER_COLOR = new Color(48,  54,  61);
    private static final Color ERROR_COLOR  = new Color(248,  81,  73);
    private static final Color SUCCESS_COLOR= new Color(35, 197, 122);

    private final UserRepository repo = new UserRepository();

    // Register panel fields
    private JTextField     regUsernameField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmField;
    private JLabel         regStatusLabel;

    // Login panel fields
    private JTextField     logUsernameField;
    private JPasswordField logPasswordField;
    private JLabel         logStatusLabel;

    public AuthFrame() {
        setTitle("Gesture Wave");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setUndecorated(false);
        setBackground(BG_DARK);

        initComponents();
    }

    // ────────────────────────────────────────────────────────────────
    //  UI Construction
    // ────────────────────────────────────────────────────────────────

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        root.add(buildHeader(), BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = buildTabbedPane();
        root.add(tabs, BorderLayout.CENTER);

        // Footer
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        // Lock icon (unicode)
        JLabel icon = new JLabel("🔐");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Gesture Wave");
        title.setFont(new Font("Georgia", Font.BOLD, 26));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Gesture Wave User Authentication");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 11));
        subtitle.setForeground(ACCENT_GREEN);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG_CARD);
        tabs.setForeground(TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TabbedPane.selected",     BG_CARD);
        UIManager.put("TabbedPane.background",   BG_DARK);
        UIManager.put("TabbedPane.foreground",   TEXT_PRIMARY);
        UIManager.put("TabbedPane.focus",        BG_CARD);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));

        tabs.addTab("  Register  ", buildRegisterPanel());
        tabs.addTab("   Login    ", buildLoginPanel());
        tabs.setOpaque(true);
        return tabs;
    }

    // ── Register Panel ───────────────────────────────────────────────

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(28, 32, 20, 32));

        panel.add(fieldLabel("Username"));
        regUsernameField = styledTextField("Choose a username");
        panel.add(regUsernameField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(fieldLabel("Password"));
        regPasswordField = styledPasswordField("Enter password");
        panel.add(regPasswordField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(fieldLabel("Confirm Password"));
        regConfirmField = styledPasswordField("Re-enter password");
        panel.add(regConfirmField);
        panel.add(Box.createVerticalStrut(22));

        JButton registerBtn = buildButton("Register Account", ACCENT_GREEN);
        registerBtn.addActionListener(e -> handleRegister());
        panel.add(registerBtn);

        panel.add(Box.createVerticalStrut(14));
        regStatusLabel = statusLabel();
        panel.add(regStatusLabel);

        // Hint about file storage
        panel.add(Box.createVerticalStrut(10));
        JLabel hint = new JLabel("Data saved to: users.dat in project folder");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 10));
        hint.setForeground(TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(hint);

        return panel;
    }

    // ── Login Panel ──────────────────────────────────────────────────

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(36, 32, 20, 32));

        panel.add(fieldLabel("Username"));
        logUsernameField = styledTextField("Enter your username");
        panel.add(logUsernameField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(fieldLabel("Password"));
        logPasswordField = styledPasswordField("Enter your password");
        panel.add(logPasswordField);
        panel.add(Box.createVerticalStrut(22));

        JButton loginBtn = buildButton("Login", ACCENT_BLUE);
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn);

        panel.add(Box.createVerticalStrut(14));
        logStatusLabel = statusLabel();
        panel.add(logStatusLabel);

        panel.add(Box.createVerticalStrut(10));
        JLabel info = new JLabel("Password verified using SHA-256 hash comparison");
        info.setFont(new Font("Monospaced", Font.PLAIN, 10));
        info.setForeground(TEXT_MUTED);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(info);

        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel label = new JLabel("All passwords encrypted with SHA-256 | Java Authentication System");
        label.setFont(new Font("Monospaced", Font.PLAIN, 10));
        label.setForeground(TEXT_MUTED);
        footer.add(label);
        return footer;
    }

    // ────────────────────────────────────────────────────────────────
    //  Logic Handlers
    // ────────────────────────────────────────────────────────────────

    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = new String(regPasswordField.getPassword());
        String confirm  = new String(regConfirmField.getPassword());

        // Client-side validation
        if (username.isEmpty()) {
            setStatus(regStatusLabel, "⚠  Username cannot be empty.", ERROR_COLOR); return;
        }
        if (password.isEmpty()) {
            setStatus(regStatusLabel, "⚠  Password cannot be empty.", ERROR_COLOR); return;
        }
        if (!password.equals(confirm)) {
            setStatus(regStatusLabel, "⚠  Passwords do not match.", ERROR_COLOR); return;
        }
        if (password.length() < 6) {
            setStatus(regStatusLabel, "⚠  Password must be at least 6 characters.", ERROR_COLOR); return;
        }

        // Delegate to repository (handles file I/O + hashing)
        UserRepository.RegisterResult result = repo.registerUser(username, password);

        switch (result) {
            case SUCCESS:
                setStatus(regStatusLabel,
                    "✔  \"" + username + "\" registered successfully!", SUCCESS_COLOR);
                clearFields(regUsernameField, regPasswordField, regConfirmField);
                break;
            case ALREADY_EXISTS:
                setStatus(regStatusLabel,
                    "✘  Username \"" + username + "\" is already taken.", ERROR_COLOR);
                break;
            case IO_ERROR:
                setStatus(regStatusLabel,
                    "✘  File error — could not save user data.", ERROR_COLOR);
                break;
            default:
                setStatus(regStatusLabel, "✘  Unexpected error.", ERROR_COLOR);
        }
    }

    private void handleLogin() {
        String username = logUsernameField.getText().trim();
        String password = new String(logPasswordField.getPassword());

        if (username.isEmpty()) {
            setStatus(logStatusLabel, "⚠  Username cannot be empty.", ERROR_COLOR); return;
        }
        if (password.isEmpty()) {
            setStatus(logStatusLabel, "⚠  Password cannot be empty.", ERROR_COLOR); return;
        }

        // Hash the input and compare against stored hash (done inside repo)
        UserRepository.LoginResult result = repo.loginUser(username, password);

        switch (result) {
            case SUCCESS:
                setStatus(logStatusLabel,
                    "✔  Welcome back, " + username + "! Login successful.", SUCCESS_COLOR);
                this.dispose();              // close login window
                dispose();
                javax.swing.SwingUtilities.invokeLater(() -> new Navigate(username));
                clearFields(logUsernameField, logPasswordField);
                break;
            case USER_NOT_FOUND:
                setStatus(logStatusLabel,
                    "✘  No account found for \"" + username + "\".", ERROR_COLOR);
                break;
            case WRONG_PASSWORD:
                setStatus(logStatusLabel,
                    "✘  Incorrect password. Hash mismatch.", ERROR_COLOR);
                break;
            default:
                setStatus(logStatusLabel, "✘  Unexpected error.", ERROR_COLOR);
        }
    }

    /**
     * Opens a simple protected dashboard dialog after successful login.
     */
    private void showDashboard(String username) {
        JDialog dialog = new JDialog(this, "Secure Dashboard", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(BG_DARK);

        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel emoji = new JLabel("🛡");
        emoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        emoji.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Access Granted");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(ACCENT_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg1 = new JLabel("Authenticated as:  " + username);
        msg1.setFont(new Font("Monospaced", Font.BOLD, 13));
        msg1.setForeground(TEXT_PRIMARY);
        msg1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg2 = new JLabel("SHA-256 hash verification passed ✔");
        msg2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        msg2.setForeground(TEXT_MUTED);
        msg2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton close = buildButton("Close Session", ACCENT_BLUE);
        close.addActionListener(e -> dialog.dispose());

        p.add(emoji);
        p.add(Box.createVerticalStrut(10));
        p.add(title);
        p.add(Box.createVerticalStrut(14));
        p.add(msg1);
        p.add(Box.createVerticalStrut(6));
        p.add(msg2);
        p.add(Box.createVerticalStrut(20));
        p.add(close);

        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    // ────────────────────────────────────────────────────────────────
    //  UI Component Factories
    // ────────────────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 2, 4, 0));
        return label;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    g2.drawString(placeholder, 10, getHeight() / 2 + 5);
                }
            }
        };
        styleInputField(field);
        return field;
    }

    private void styleInputField(JTextField field) {
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GREEN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Focus highlight
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT_BLUE, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
                ));
            }
        });
    }

    private JButton buildButton(String text, Color accentColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()
                    ? accentColor.darker()
                    : getModel().isRollover() ? accentColor.brighter() : accentColor;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(BG_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(BG_DARK);
        btn.setBackground(accentColor);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private JLabel statusLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void setStatus(JLabel label, String message, Color color) {
        label.setText(message);
        label.setForeground(color);
    }

    private void clearFields(JComponent... fields) {
        for (JComponent f : fields) {
            if (f instanceof JTextField)     ((JTextField)     f).setText("");
            if (f instanceof JPasswordField) ((JPasswordField) f).setText("");
        }
    }
}
