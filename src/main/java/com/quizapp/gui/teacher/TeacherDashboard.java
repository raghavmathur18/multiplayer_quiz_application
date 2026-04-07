package com.quizapp.gui.teacher;

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
 * TeacherDashboard - main window for teacher users.
 * Allows class management, quiz creation, and viewing analytics.
 */
public class TeacherDashboard extends JFrame {
    private User teacher;
    private ClassDAO classDAO = new ClassDAO();
    private QuizDAO quizDAO = new QuizDAO();
    private ResultDAO resultDAO = new ResultDAO();

    private JPanel mainContent;
    private CardLayout cardLayout;

    // Class panel components
    private DefaultListModel<ClassRoom> classListModel = new DefaultListModel<>();
    private JList<ClassRoom> classList = new JList<>(classListModel);
    private JPanel classDetailPanel;
    private ClassRoom selectedClass;

    // Quiz panel components
    private DefaultListModel<Quiz> quizListModel = new DefaultListModel<>();
    private JList<Quiz> quizList = new JList<>(quizListModel);

    // Analytics panel
    private JPanel analyticsHolder;

    public TeacherDashboard(User teacher) {
        this.teacher = teacher;
        UITheme.applyGlobalTheme();
        initUI();
        loadClasses();
    }

    private void initUI() {
        setTitle("QuizArena — Teacher: " + teacher.getName());
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

        addSidebarLogo(sb);
        sb.add(Box.createVerticalStrut(20));
        addUserCard(sb);
        sb.add(Box.createVerticalStrut(28));

        addNavBtn(sb, "📚   My Classes", "CLASSES");
        sb.add(Box.createVerticalStrut(6));
        addNavBtn(sb, "📊   Analytics", "ANALYTICS");
        sb.add(Box.createVerticalGlue());

        JButton logout = UITheme.createDangerButton("⎋  Logout");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> { dispose(); new LoginFrame(); });
        sb.add(logout);
        return sb;
    }

    private void addSidebarLogo(JPanel sb) {
        JLabel logo = new JLabel("🎯  QuizArena");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(UITheme.PRIMARY_LIGHT);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(logo);
        JLabel sub = new JLabel("Teacher Portal");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sb.add(sub);
    }

    private void addUserCard(JPanel sb) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0; card.add(UITheme.createLabel(teacher.getName(), UITheme.FONT_SUBHEADING, UITheme.TEXT_PRIMARY), g);
        g.gridy = 1; card.add(UITheme.createLabel(teacher.getEmail(), UITheme.FONT_SMALL, UITheme.TEXT_MUTED), g);
        sb.add(card);
    }

    private void addNavBtn(JPanel sb, String text, String card) {
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
            cardLayout.show(mainContent, card);
            if ("ANALYTICS".equals(card)) refreshAnalytics();
        });
        sb.add(btn);
    }

    // ─────────────────────────────────────────────────────────
    // CLASSES PANEL (left: class list, right: detail)
    // ─────────────────────────────────────────────────────────
    private JPanel buildClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_CONTENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_CONTENT);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
        JLabel title = UITheme.createLabel("Class Management", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JButton newClassBtn = UITheme.createPrimaryButton("+ New Class");
        newClassBtn.addActionListener(e -> showCreateClassDialog());
        header.add(title, BorderLayout.WEST);
        header.add(newClassBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(280);
        split.setDividerSize(2);
        split.setBackground(UITheme.BORDER);

        // Left: class list
        split.setLeftComponent(buildClassListPanel());

        // Right: detail placeholder
        classDetailPanel = buildWelcomeDetail();
        split.setRightComponent(classDetailPanel);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildClassListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        classList.setBackground(UITheme.BG_SIDEBAR);
        classList.setForeground(UITheme.TEXT_PRIMARY);
        classList.setFont(UITheme.FONT_BODY);
        classList.setSelectionBackground(UITheme.PRIMARY);
        classList.setSelectionForeground(Color.WHITE);
        classList.setFixedCellHeight(52);
        classList.setBorder(BorderFactory.createEmptyBorder());
        classList.setCellRenderer(new ClassListCellRenderer());
        classList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && classList.getSelectedValue() != null) {
                selectedClass = classList.getSelectedValue();
                showClassDetail(selectedClass);
            }
        });
        p.add(UITheme.createScrollPane(classList), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildWelcomeDetail() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG_CONTENT);
        JLabel lbl = UITheme.createLabel("← Select or create a class", UITheme.FONT_HEADING, UITheme.TEXT_MUTED);
        p.add(lbl);
        return p;
    }

    private void showClassDetail(ClassRoom cr) {
        JPanel detail = new JPanel(new BorderLayout());
        detail.setBackground(UITheme.BG_CONTENT);
        detail.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Class info header
        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(UITheme.BG_CONTENT);
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setBackground(UITheme.BG_CONTENT);
        JLabel cName = UITheme.createLabel(cr.getClassName(), UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        JLabel cCode = UITheme.createLabel("  Code: " + cr.getClassCode(), UITheme.FONT_BODY, UITheme.PRIMARY_LIGHT);
        titleRow.add(cName); titleRow.add(cCode);
        infoBar.add(titleRow, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setBackground(UITheme.BG_CONTENT);
        JButton addQuizBtn = UITheme.createPrimaryButton("+ New Quiz");
        JButton deleteClassBtn = UITheme.createDangerButton("Delete Class");
        addQuizBtn.addActionListener(e -> showCreateQuizDialog(cr));
        deleteClassBtn.addActionListener(e -> {
            if (UITheme.showConfirm(this, "Delete Class", "Delete class '" + cr.getClassName() + "'? This cannot be undone.")) {
                classDAO.deleteClass(cr.getClassId());
                loadClasses();
                classDetailPanel.removeAll();
                classDetailPanel.add(buildWelcomeDetail());
                classDetailPanel.revalidate();
            }
        });
        actionBtns.add(addQuizBtn); actionBtns.add(deleteClassBtn);
        infoBar.add(actionBtns, BorderLayout.EAST);
        detail.add(infoBar, BorderLayout.NORTH);

        // Tabs: Students | Quizzes
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEADING);
        tabs.setBackground(UITheme.BG_CONTENT);
        tabs.setForeground(UITheme.TEXT_PRIMARY);
        tabs.addTab("👥  Students", buildStudentListTab(cr));
        tabs.addTab("📝  Quizzes", buildQuizListTab(cr));
        detail.add(tabs, BorderLayout.CENTER);

        // Replace right panel in split pane
        Container parent = classDetailPanel.getParent();
        if (parent instanceof JSplitPane sp) {
            int loc = sp.getDividerLocation();
            sp.setRightComponent(detail);
            sp.setDividerLocation(loc);
            classDetailPanel = detail;
        }
    }

    private JPanel buildStudentListTab(ClassRoom cr) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_CONTENT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        List<User> students = classDAO.getClassStudents(cr.getClassId());
        String[] cols = {"#", "Name", "Email", "Action"};
        Object[][] data = new Object[students.size()][4];
        for (int i = 0; i < students.size(); i++) {
            data[i][0] = i + 1;
            data[i][1] = students.get(i).getName();
            data[i][2] = students.get(i).getEmail();
            data[i][3] = "Remove";
        }
        JTable table = buildStyledTable(data, cols);
        p.add(UITheme.createScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(UITheme.BG_CONTENT);
        JLabel info = UITheme.createLabel("Share class code: " + cr.getClassCode() + " with students to join.", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        bottom.add(info);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildQuizListTab(ClassRoom cr) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_CONTENT);
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        List<Quiz> quizzes = quizDAO.getQuizzesByClass(cr.getClassId());
        JPanel quizCards = new JPanel();
        quizCards.setLayout(new BoxLayout(quizCards, BoxLayout.Y_AXIS));
        quizCards.setBackground(UITheme.BG_CONTENT);

        for (Quiz q : quizzes) {
            quizCards.add(buildQuizCard(q, cr));
            quizCards.add(Box.createVerticalStrut(10));
        }
        if (quizzes.isEmpty()) {
            JLabel empty = UITheme.createLabel("No quizzes yet. Click '+ New Quiz' to create one.", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            quizCards.add(Box.createVerticalStrut(60));
            quizCards.add(empty);
        }
        p.add(UITheme.createScrollPane(quizCards), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildQuizCard(Quiz quiz, ClassRoom cr) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(UITheme.BG_CARD);
        List<Question> qs = quizDAO.getQuestionsByQuiz(quiz.getQuizId());
        JLabel titleLbl = UITheme.createLabel(quiz.getQuizTitle(), UITheme.FONT_SUBHEADING, UITheme.TEXT_PRIMARY);
        JLabel meta = UITheme.createLabel(qs.size() + " questions  •  " + quiz.getTimeLimitMinutes() + " min  •  " +
            (quiz.isActive() ? "🔴 LIVE" : "⚫ Inactive"), UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY);
        left.add(titleLbl); left.add(meta);
        card.add(left, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setBackground(UITheme.BG_CARD);
        JButton startStop = quiz.isActive() ? UITheme.createDangerButton("Stop Quiz") : UITheme.createSuccessButton("Start Quiz");
        JButton viewBtn = UITheme.createPrimaryButton("Edit / View");
        JButton deleteBtn = UITheme.createDangerButton("Delete");

        startStop.setPreferredSize(new Dimension(110, 34));
        viewBtn.setPreferredSize(new Dimension(110, 34));
        deleteBtn.setPreferredSize(new Dimension(90, 34));

        startStop.addActionListener(e -> {
            boolean nowActive = !quiz.isActive();
            quizDAO.setQuizActive(quiz.getQuizId(), nowActive);
            quiz.setActive(nowActive);
            UITheme.showInfo(this, "Quiz " + (nowActive ? "Started" : "Stopped"),
                "Quiz '" + quiz.getQuizTitle() + "' is now " + (nowActive ? "LIVE" : "inactive") + ".");
            showClassDetail(cr);
        });
        viewBtn.addActionListener(e -> new QuizEditorFrame(quiz, teacher, () -> showClassDetail(cr)));
        deleteBtn.addActionListener(e -> {
            if (UITheme.showConfirm(this, "Delete Quiz", "Delete quiz '" + quiz.getQuizTitle() + "'?")) {
                quizDAO.deleteQuiz(quiz.getQuizId());
                showClassDetail(cr);
            }
        });
        btns.add(startStop); btns.add(viewBtn); btns.add(deleteBtn);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    // ─────────────────────────────────────────────────────────
    // ANALYTICS PANEL
    // ─────────────────────────────────────────────────────────
    private JPanel buildAnalyticsPanel() {
        analyticsHolder = new JPanel(new BorderLayout());
        analyticsHolder.setBackground(UITheme.BG_CONTENT);
        JLabel placeholder = UITheme.createLabel("Select 'Analytics' from the menu to load charts.", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
        analyticsHolder.add(placeholder, BorderLayout.CENTER);
        return analyticsHolder;
    }

    private void refreshAnalytics() {
        analyticsHolder.removeAll();
        List<ClassRoom> classes = classDAO.getTeacherClasses(teacher.getUserId());
        if (classes.isEmpty()) {
            analyticsHolder.add(UITheme.createLabel("No classes to show analytics for.", UITheme.FONT_BODY, UITheme.TEXT_MUTED), BorderLayout.CENTER);
            analyticsHolder.revalidate(); analyticsHolder.repaint();
            return;
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_CONTENT);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel heading = UITheme.createLabel("Analytics Overview", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(16));

        for (ClassRoom cr : classes) {
            List<Quiz> quizzes = quizDAO.getQuizzesByClass(cr.getClassId());
            if (quizzes.isEmpty()) continue;

            JLabel classLabel = UITheme.createLabel("Class: " + cr.getClassName(), UITheme.FONT_HEADING, UITheme.PRIMARY_LIGHT);
            classLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(classLabel);
            content.add(Box.createVerticalStrut(10));

            for (Quiz q : quizzes) {
                List<Result> results = resultDAO.getQuizResults(q.getQuizId());
                if (results.isEmpty()) continue;

                JLabel qLabel = UITheme.createLabel("Quiz: " + q.getQuizTitle(), UITheme.FONT_SUBHEADING, UITheme.TEXT_SECONDARY);
                qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(qLabel);
                content.add(Box.createVerticalStrut(6));

                // Stats summary
                double avg = results.stream().mapToDouble(r -> r.getScore() * 100.0 / r.getTotalQuestions()).average().orElse(0);
                double max = results.stream().mapToDouble(r -> r.getScore() * 100.0 / r.getTotalQuestions()).max().orElse(0);
                JLabel statsLabel = UITheme.createLabel(
                    String.format("Students: %d  |  Avg Score: %.1f%%  |  Highest: %.1f%%", results.size(), avg, max),
                    UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
                statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(statsLabel);
                content.add(Box.createVerticalStrut(8));

                // Charts row
                JPanel chartsRow = new JPanel(new GridLayout(1, 3, 12, 0));
                chartsRow.setBackground(UITheme.BG_CONTENT);
                chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
                chartsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                ChartPanel bar = AnalyticsPanel.createClassPerformanceBarChart(results, q.getQuizTitle());
                int[] ci = resultDAO.getCorrectIncorrectCount(q.getQuizId());
                ChartPanel pie = AnalyticsPanel.createCorrectIncorrectPieChart(ci[0], ci[1], "Correct vs Incorrect");
                ChartPanel trend = AnalyticsPanel.createPerformanceTrendLineChart(results, "Students");

                chartsRow.add(bar); chartsRow.add(pie); chartsRow.add(trend);
                content.add(chartsRow);
                content.add(Box.createVerticalStrut(20));

                // Rankings table
                content.add(buildRankingsTable(results));
                content.add(Box.createVerticalStrut(24));
            }
        }

        analyticsHolder.add(UITheme.createScrollPane(content), BorderLayout.CENTER);
        analyticsHolder.revalidate();
        analyticsHolder.repaint();
    }

    private JPanel buildRankingsTable(List<Result> results) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.BG_CONTENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = UITheme.createLabel("Rankings", UITheme.FONT_SUBHEADING, UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"Rank", "Student", "Score", "Percentage", "Time (sec)"};
        Object[][] data = new Object[results.size()][5];
        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);
            data[i][0] = "#" + (i + 1);
            data[i][1] = r.getStudentName();
            data[i][2] = r.getScore() + " / " + r.getTotalQuestions();
            data[i][3] = String.format("%.1f%%", r.getPercentage());
            data[i][4] = r.getTimeTakenSeconds();
        }
        p.add(UITheme.createScrollPane(buildStyledTable(data, cols)), BorderLayout.CENTER);
        return p;
    }

    // ─────────────────────────────────────────────────────────
    // DIALOGS
    // ─────────────────────────────────────────────────────────
    private void showCreateClassDialog() {
        JDialog dialog = new JDialog(this, "Create New Class", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(UITheme.BG_CARD);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        g.gridy = 0; panel.add(UITheme.createLabel("Create New Class", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), g);
        g.gridy = 1; panel.add(UITheme.createLabel("Class Name *", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 2; JTextField nameField = UITheme.createTextField("e.g. Mathematics 101"); panel.add(nameField, g);
        g.gridy = 3; panel.add(UITheme.createLabel("Description (optional)", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 4;
        JTextArea desc = UITheme.createTextArea(2, 30);
        panel.add(UITheme.createScrollPane(desc), g);
        g.gridy = 5; g.insets = new Insets(16, 0, 0, 0);
        JButton create = UITheme.createPrimaryButton("Create Class");
        panel.add(create, g);

        create.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { UITheme.showError(dialog, "Error", "Class name is required."); return; }
            ClassRoom cr = classDAO.createClass(name, teacher.getUserId(), desc.getText().trim());
            if (cr != null) {
                UITheme.showInfo(dialog, "Created!", "Class created! Code: " + cr.getClassCode());
                dialog.dispose();
                loadClasses();
            } else {
                UITheme.showError(dialog, "Error", "Failed to create class.");
            }
        });
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCreateQuizDialog(ClassRoom cr) {
        JDialog dialog = new JDialog(this, "Create New Quiz", true);
        dialog.setSize(440, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        g.gridy = 0; panel.add(UITheme.createLabel("Create Quiz for: " + cr.getClassName(), UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), g);
        g.gridy = 1; panel.add(UITheme.createLabel("Quiz Title *", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 2; JTextField titleField = UITheme.createTextField("e.g. Chapter 1 Quiz"); panel.add(titleField, g);
        g.gridy = 3; panel.add(UITheme.createLabel("Time Limit (minutes)", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 4;
        JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 180, 5));
        timeSpinner.setBackground(UITheme.BG_INPUT);
        timeSpinner.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(timeSpinner, g);
        g.gridy = 5; g.insets = new Insets(16, 0, 0, 0);
        JButton create = UITheme.createPrimaryButton("Create & Add Questions");
        panel.add(create, g);

        create.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) { UITheme.showError(dialog, "Error", "Quiz title is required."); return; }
            int timeLimit = (Integer) timeSpinner.getValue();
            Quiz quiz = quizDAO.createQuiz(title, cr.getClassId(), timeLimit);
            if (quiz != null) {
                dialog.dispose();
                new QuizEditorFrame(quiz, teacher, () -> showClassDetail(cr));
            } else {
                UITheme.showError(dialog, "Error", "Failed to create quiz.");
            }
        });
        dialog.add(panel);
        dialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────
    private void loadClasses() {
        classListModel.clear();
        List<ClassRoom> classes = classDAO.getTeacherClasses(teacher.getUserId());
        for (ClassRoom cr : classes) classListModel.addElement(cr);
    }

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
        table.setSelectionBackground(UITheme.PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(UITheme.BG_SIDEBAR);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(UITheme.FONT_SMALL);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                comp.setBackground(sel ? UITheme.PRIMARY : (r % 2 == 0 ? UITheme.BG_CARD : UITheme.BG_CONTENT));
                comp.setForeground(sel ? Color.WHITE : UITheme.TEXT_PRIMARY);
                ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return comp;
            }
        });
        return table;
    }

    // Cell renderer for class list
    static class ClassListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cell = new JPanel(new GridLayout(2, 1));
            cell.setBackground(isSelected ? UITheme.PRIMARY : UITheme.BG_SIDEBAR);
            cell.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            ClassRoom cr = (ClassRoom) value;
            JLabel name = new JLabel(cr.getClassName());
            name.setFont(UITheme.FONT_SUBHEADING);
            name.setForeground(isSelected ? Color.WHITE : UITheme.TEXT_PRIMARY);
            JLabel code = new JLabel("Code: " + cr.getClassCode());
            code.setFont(UITheme.FONT_SMALL);
            code.setForeground(isSelected ? new Color(200, 220, 255) : UITheme.TEXT_MUTED);
            cell.add(name); cell.add(code);
            return cell;
        }
    }
}
