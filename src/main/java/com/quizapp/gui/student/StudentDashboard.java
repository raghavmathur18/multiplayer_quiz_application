package com.quizapp.gui.student;

import com.quizapp.database.*;
import com.quizapp.gui.common.*;
import com.quizapp.models.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * StudentDashboard — main window for student users.
 * Shows enrolled classes, available quizzes, and performance analytics.
 */
public class StudentDashboard extends JFrame {
    private User student;
    private ClassDAO classDAO = new ClassDAO();
    private QuizDAO quizDAO = new QuizDAO();
    private ResultDAO resultDAO = new ResultDAO();

    private JPanel mainContent;
    private CardLayout cardLayout;
    private JPanel analyticsHolder;

    public StudentDashboard(User student) {
        this.student = student;
        UITheme.applyGlobalTheme();
        initUI();
    }

    private void initUI() {
        setTitle("QuizArena — Student: " + student.getName());
        setSize(1280, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);
        root.add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(UITheme.BG_CONTENT);
        mainContent.add(buildClassesPanel(), "CLASSES");
        mainContent.add(buildAnalyticsPanel(), "ANALYTICS");
        root.add(mainContent, BorderLayout.CENTER);

        add(root);
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(UITheme.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        JLabel logo = new JLabel("🎯  QuizArena");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(UITheme.STUDENT_ACCENT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(logo);
        JLabel sub = new JLabel("Student Portal");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(sub);
        sb.add(Box.createVerticalStrut(20));

        // User card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0; card.add(UITheme.createLabel(student.getName(), UITheme.FONT_SUBHEADING, UITheme.TEXT_PRIMARY), g);
        g.gridy = 1; card.add(UITheme.createLabel(student.getEmail(), UITheme.FONT_SMALL, UITheme.TEXT_MUTED), g);
        sb.add(card);
        sb.add(Box.createVerticalStrut(28));

        addNavBtn(sb, "📚   My Classes", "CLASSES");
        sb.add(Box.createVerticalStrut(6));
        addNavBtn(sb, "📊   My Analytics", "ANALYTICS");
        sb.add(Box.createVerticalGlue());

        JButton joinBtn = UITheme.createSuccessButton("+ Join Class");
        joinBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        joinBtn.addActionListener(e -> showJoinClassDialog());
        sb.add(joinBtn);
        sb.add(Box.createVerticalStrut(8));

        JButton logout = UITheme.createDangerButton("⎋  Logout");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        sb.add(logout);
        return sb;
    }

    private void addNavBtn(JPanel sb, String text, String key) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(UITheme.TEXT_SECONDARY);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(UITheme.TEXT_PRIMARY); btn.setBackground(UITheme.BG_CARD); }
            public void mouseExited(MouseEvent e) { btn.setForeground(UITheme.TEXT_SECONDARY); btn.setBackground(UITheme.BG_SIDEBAR); }
        });
        btn.addActionListener(e -> {
            cardLayout.show(mainContent, key);
            if ("ANALYTICS".equals(key)) refreshAnalytics();
            if ("CLASSES".equals(key)) refreshClasses();
        });
        sb.add(btn);
    }

    // ─────────────────────────────────────────────────────────
    // CLASSES PANEL
    // ─────────────────────────────────────────────────────────
    private JPanel buildClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_CONTENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_CONTENT);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        header.add(UITheme.createLabel("My Classes & Quizzes", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY), BorderLayout.WEST);

        JPanel classCards = new JPanel();
        classCards.setLayout(new BoxLayout(classCards, BoxLayout.Y_AXIS));
        classCards.setBackground(UITheme.BG_CONTENT);
        classCards.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        classCards.setName("classCards");

        panel.add(header, BorderLayout.NORTH);
        panel.add(UITheme.createScrollPane(classCards), BorderLayout.CENTER);

        // Load classes into cards panel
        loadClassCards(classCards);
        return panel;
    }

    private void loadClassCards(JPanel classCards) {
        classCards.removeAll();
        List<ClassRoom> classes = classDAO.getStudentClasses(student.getUserId());

        if (classes.isEmpty()) {
            classCards.add(Box.createVerticalStrut(60));
            JLabel empty = UITheme.createLabel("You haven't joined any classes yet. Click '+ Join Class' to get started.", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            classCards.add(empty);
        }

        for (ClassRoom cr : classes) {
            classCards.add(buildClassCard(cr));
            classCards.add(Box.createVerticalStrut(16));
        }
        classCards.revalidate();
        classCards.repaint();
    }

    private void refreshClasses() {
        for (Component c : mainContent.getComponents()) {
            if (c instanceof JScrollPane sp && sp.getViewport().getView() instanceof JPanel inner) {
                if ("classCards".equals(inner.getName())) {
                    loadClassCards(inner);
                    return;
                }
            }
        }
        // Rebuild
        mainContent.remove(0);
        mainContent.add(buildClassesPanel(), "CLASSES", 0);
        cardLayout.show(mainContent, "CLASSES");
    }

    private JPanel buildClassCard(ClassRoom cr) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        // Class header
        JPanel classHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        classHeader.setBackground(UITheme.BG_CARD);
        classHeader.add(UITheme.createLabel(cr.getClassName(), UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY));
        classHeader.add(UITheme.createLabel("Teacher: " + cr.getTeacherName() + "  |  Code: " + cr.getClassCode(),
            UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        card.add(classHeader, BorderLayout.NORTH);

        // Quizzes
        List<Quiz> activeQuizzes = quizDAO.getAllActiveQuizzesForStudent(student.getUserId(), cr.getClassId());
        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBackground(UITheme.BG_CARD);
        quizPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        if (activeQuizzes.isEmpty()) {
            quizPanel.add(UITheme.createLabel("No active quizzes at this time.", UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        }

        for (Quiz q : activeQuizzes) {
            quizPanel.add(buildQuizRow(q, cr));
            quizPanel.add(Box.createVerticalStrut(6));
        }
        card.add(quizPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuizRow(Quiz quiz, ClassRoom cr) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(40, 55, 78));
        row.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        boolean attempted = resultDAO.hasAttemptedQuiz(student.getUserId(), quiz.getQuizId());
        List<Question> qs = quizDAO.getQuestionsByQuiz(quiz.getQuizId());

        JPanel left = new JPanel(new GridLayout(1, 2, 20, 0));
        left.setBackground(new Color(40, 55, 78));
        left.add(UITheme.createLabel("📝  " + quiz.getQuizTitle(), UITheme.FONT_BODY, UITheme.TEXT_PRIMARY));
        left.add(UITheme.createLabel(qs.size() + " Qs  |  " + quiz.getTimeLimitMinutes() + " min", UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        row.add(left, BorderLayout.CENTER);

        if (attempted) {
            JLabel doneLbl = UITheme.createLabel("✓ Completed", UITheme.FONT_SMALL, UITheme.SECONDARY);
            row.add(doneLbl, BorderLayout.EAST);
        } else {
            JButton startBtn = UITheme.createSuccessButton("Start Quiz");
            startBtn.setPreferredSize(new Dimension(110, 32));
            startBtn.addActionListener(e -> {
                if (qs.isEmpty()) {
                    UITheme.showError(this, "No Questions", "This quiz has no questions yet.");
                    return;
                }
                new QuizAttemptFrame(quiz, student, () -> refreshClasses());
            });
            row.add(startBtn, BorderLayout.EAST);
        }
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // ANALYTICS PANEL
    // ─────────────────────────────────────────────────────────
    private JPanel buildAnalyticsPanel() {
        analyticsHolder = new JPanel(new BorderLayout());
        analyticsHolder.setBackground(UITheme.BG_CONTENT);
        return analyticsHolder;
    }

    private void refreshAnalytics() {
        analyticsHolder.removeAll();

        List<Result> results = resultDAO.getStudentResults(student.getUserId());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_CONTENT);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel heading = UITheme.createLabel("My Performance Analytics", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(8));

        if (results.isEmpty()) {
            content.add(UITheme.createLabel("No quiz attempts yet. Complete a quiz to see analytics!", UITheme.FONT_BODY, UITheme.TEXT_MUTED));
            analyticsHolder.add(UITheme.createScrollPane(content), BorderLayout.CENTER);
            analyticsHolder.revalidate();
            return;
        }

        // Summary stats
        double avg = results.stream().mapToDouble(Result::getPercentage).average().orElse(0);
        double best = results.stream().mapToDouble(Result::getPercentage).max().orElse(0);
        JLabel statsLabel = UITheme.createLabel(
            String.format("Quizzes Taken: %d  |  Average: %.1f%%  |  Best: %.1f%%", results.size(), avg, best),
            UITheme.FONT_BODY, UITheme.TEXT_SECONDARY);
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statsLabel);
        content.add(Box.createVerticalStrut(20));

        // Charts row
        JPanel chartsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        chartsRow.setBackground(UITheme.BG_CONTENT);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        chartsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        ChartPanel trend = AnalyticsPanel.createPerformanceTrendLineChart(results, student.getName());
        ChartPanel scores = AnalyticsPanel.createStudentQuizScoresBarChart(results);
        int[] ci = resultDAO.getStudentCorrectIncorrect(student.getUserId());
        ChartPanel pie = AnalyticsPanel.createCorrectIncorrectPieChart(ci[0], ci[1], "Correct vs Incorrect");

        chartsRow.add(trend);
        chartsRow.add(scores);
        chartsRow.add(pie);
        content.add(chartsRow);
        content.add(Box.createVerticalStrut(24));

        // Results table
        JLabel tableTitle = UITheme.createLabel("Quiz History", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY);
        tableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(tableTitle);
        content.add(Box.createVerticalStrut(8));

        String[] cols = {"Quiz", "Score", "Percentage", "Time (sec)", "Date"};
        Object[][] data = new Object[results.size()][5];
        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);
            data[i][0] = r.getQuizTitle();
            data[i][1] = r.getScore() + " / " + r.getTotalQuestions();
            data[i][2] = String.format("%.1f%%", r.getPercentage());
            data[i][3] = r.getTimeTakenSeconds();
            data[i][4] = r.getAttemptedAt() != null ? r.getAttemptedAt().toString().substring(0, 16) : "N/A";
        }

        JTable table = buildStyledTable(data, cols);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(UITheme.BG_CONTENT);
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tablePanel.add(UITheme.createScrollPane(table), BorderLayout.CENTER);
        content.add(tablePanel);

        analyticsHolder.add(UITheme.createScrollPane(content), BorderLayout.CENTER);
        analyticsHolder.revalidate();
        analyticsHolder.repaint();
    }

    // ─────────────────────────────────────────────────────────
    // JOIN CLASS DIALOG
    // ─────────────────────────────────────────────────────────
    private void showJoinClassDialog() {
        JDialog dialog = new JDialog(this, "Join a Class", true);
        dialog.setSize(400, 240);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        g.gridy = 0; panel.add(UITheme.createLabel("Join a Class", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), g);
        g.gridy = 1; panel.add(UITheme.createLabel("Enter the 6-character class code:", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 2;
        JTextField codeField = UITheme.createTextField("e.g. ABC123");
        codeField.setFont(new Font("Consolas", Font.BOLD, 18));
        panel.add(codeField, g);
        g.gridy = 3; g.insets = new Insets(18, 0, 0, 0);
        JButton joinBtn = UITheme.createSuccessButton("Join Class");
        panel.add(joinBtn, g);

        joinBtn.addActionListener(e -> {
            String code = codeField.getText().trim().toUpperCase();
            if (code.length() != 6) {
                UITheme.showError(dialog, "Invalid Code", "Please enter a valid 6-character class code.");
                return;
            }
            ClassRoom cr = classDAO.getClassByCode(code);
            if (cr == null) {
                UITheme.showError(dialog, "Not Found", "No class found with code: " + code);
                return;
            }
            if (classDAO.isStudentEnrolled(student.getUserId(), cr.getClassId())) {
                UITheme.showInfo(dialog, "Already Enrolled", "You are already in class: " + cr.getClassName());
                dialog.dispose();
                return;
            }
            boolean joined = classDAO.joinClass(student.getUserId(), cr.getClassId());
            if (joined) {
                UITheme.showInfo(dialog, "Joined!", "Successfully joined: " + cr.getClassName());
                dialog.dispose();
                refreshClasses();
            } else {
                UITheme.showError(dialog, "Error", "Failed to join class. Try again.");
            }
        });

        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────
    private JTable buildStyledTable(Object[][] data, String[] cols) {
        JTable table = new JTable(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(UITheme.STUDENT_ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(UITheme.BG_SIDEBAR);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(UITheme.FONT_SMALL);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? UITheme.STUDENT_ACCENT : (r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_CONTENT));
                comp.setForeground(sel ? Color.WHITE : UITheme.TEXT_PRIMARY);
                ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });
        return table;
    }
}
