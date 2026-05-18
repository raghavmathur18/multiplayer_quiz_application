package com.quizapp.gui.common;

import com.quizapp.database.UserDAO;
import com.quizapp.gui.student.StudentDashboard;
import com.quizapp.gui.teacher.TeacherDashboard;
import com.quizapp.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login & Registration window — the entry point GUI for all users.
 */
public class LoginFrame extends JFrame {
    private UserDAO userDAO;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JComboBox<String> roleCombo;
    private JPanel loginPanel, registerPanel;
    private boolean showingLogin = true;

    public LoginFrame() {
        userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        setTitle("QuizArena — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.BG_DARK, getWidth(), getHeight(), new Color(30, 27, 75));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Header
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(40, 30, 10, 30));
        JLabel logo = new JLabel("🎯 QuizArena");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(UITheme.PRIMARY_LIGHT);
        JLabel tagline = new JLabel("Real-Time Multiplayer Quiz Competition");
        tagline.setFont(UITheme.FONT_SMALL);
        tagline.setForeground(UITheme.TEXT_SECONDARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; header.add(logo, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(4, 0, 0, 0); header.add(tagline, gbc);
        root.add(header, BorderLayout.NORTH);

        // Tab switcher
        JPanel tabPanel = new JPanel(new GridLayout(1, 2));
        tabPanel.setOpaque(false);
        tabPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 0, 40));

        JButton loginTab = new JButton("Login");
        JButton registerTab = new JButton("Register");
        styleTab(loginTab, true);
        styleTab(registerTab, false);

        tabPanel.add(loginTab);
        tabPanel.add(registerTab);

        // Card area
        JPanel cardArea = new JPanel(new CardLayout());
        cardArea.setOpaque(false);
        cardArea.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        loginPanel = buildLoginPanel();
        registerPanel = buildRegisterPanel();
        cardArea.add(loginPanel, "login");
        cardArea.add(registerPanel, "register");

        CardLayout cl = (CardLayout) cardArea.getLayout();

        loginTab.addActionListener(e -> {
            cl.show(cardArea, "login");
            styleTab(loginTab, true);
            styleTab(registerTab, false);
        });
        registerTab.addActionListener(e -> {
            cl.show(cardArea, "register");
            styleTab(loginTab, false);
            styleTab(registerTab, true);
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(tabPanel, BorderLayout.NORTH);
        center.add(cardArea, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        add(root);
        setVisible(true);
    }

    private void styleTab(JButton btn, boolean active) {
        btn.setFont(UITheme.FONT_BUTTON);
        btn.setForeground(active ? UITheme.TEXT_PRIMARY : UITheme.TEXT_SECONDARY);
        btn.setBackground(active ? UITheme.BG_CARD : new Color(15, 23, 42));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
    }

    private JPanel buildLoginPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridy = 0;
        JLabel heading = UITheme.createLabel("Welcome Back", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);
        card.add(heading, gbc);

        gbc.gridy = 1;
        card.add(UITheme.createLabel("Email Address", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 2;
        emailField = UITheme.createTextField("Email");
        card.add(emailField, gbc);

        gbc.gridy = 3;
        card.add(UITheme.createLabel("Password", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);

        gbc.gridy = 4;
        passwordField = UITheme.createPasswordField();
        card.add(passwordField, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(20, 0, 8, 0);
        JButton loginBtn = UITheme.createPrimaryButton("Login  →");
        loginBtn.setPreferredSize(new Dimension(0, 44));
        card.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        return card;
    }

    private JPanel buildRegisterPanel() {
        JPanel card = UITheme.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridy = 0;
        card.add(UITheme.createLabel("Create Account", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), gbc);

        gbc.gridy = 1;
        card.add(UITheme.createLabel("Full Name", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);
        gbc.gridy = 2;
        nameField = UITheme.createTextField("Full Name");
        card.add(nameField, gbc);

        gbc.gridy = 3;
        card.add(UITheme.createLabel("Email Address", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);
        gbc.gridy = 4;
        JTextField regEmail = UITheme.createTextField("Email");
        card.add(regEmail, gbc);

        gbc.gridy = 5;
        card.add(UITheme.createLabel("Password", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);
        gbc.gridy = 6;
        JPasswordField regPass = UITheme.createPasswordField();
        card.add(regPass, gbc);

        gbc.gridy = 7;
        card.add(UITheme.createLabel("Role", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), gbc);
        gbc.gridy = 8;
        roleCombo = UITheme.createComboBox(new String[]{"student", "teacher"});
        card.add(roleCombo, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(16, 0, 8, 0);
        JButton regBtn = UITheme.createSuccessButton("Create Account  →");
        regBtn.setPreferredSize(new Dimension(0, 44));
        card.add(regBtn, gbc);

        regBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = regEmail.getText().trim();
            String pass = new String(regPass.getPassword()).trim();
            String role = (String) roleCombo.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                UITheme.showError(this, "Validation Error", "All fields are required.");
                return;
            }
            if (!email.contains("@")) {
                UITheme.showError(this, "Validation Error", "Please enter a valid email.");
                return;
            }
            if (pass.length() < 4) {
                UITheme.showError(this, "Validation Error", "Password must be at least 4 characters.");
                return;
            }
            if (userDAO.emailExists(email)) {
                UITheme.showError(this, "Registration Error", "Email already registered.");
                return;
            }

            User user = userDAO.register(name, email, pass, role);
            if (user != null) {
                UITheme.showInfo(this, "Success", "Account created! Please login.");
            } else {
                UITheme.showError(this, "Error", "Registration failed. Try again.");
            }
        });

        return card;
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            UITheme.showError(this, "Validation Error", "Email and password are required.");
            return;
        }

        User user = userDAO.login(email, password);
        if (user == null) {
            UITheme.showError(this, "Login Failed", "Invalid email or password.");
            passwordField.setText("");
            return;
        }

        dispose();
        SwingUtilities.invokeLater(() -> {
            if (user.isTeacher()) {
                new TeacherDashboard(user);
            } else {
                new StudentDashboard(user);
            }
        });
    }
}
