package com.quizapp.gui.student;

import com.quizapp.client.QuizClient;
import com.quizapp.database.*;
import com.quizapp.database.DatabaseConfig;
import com.quizapp.gui.common.UITheme;
import com.quizapp.models.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * QuizAttemptFrame — the real-time quiz interface for students.
 * Connects to QuizServer via socket, shows questions, timer, and submits answers.
 */
public class QuizAttemptFrame extends JFrame {
    private Quiz quiz;
    private User student;
    private Runnable onComplete;
    private List<Question> questions;

    // Socket client
    private QuizClient quizClient;
    private boolean useSocket = true;

    // UI
    private JPanel questionContainer;
    private JLabel timerLabel;
    private JLabel progressLabel;
    private JLabel statusLabel;
    private JButton submitBtn;
    private JPanel[] questionPanels;
    private ButtonGroup[] optionGroups;
    private JRadioButton[][] optionButtons;

    // State
    private int totalSeconds;
    private Timer countdownTimer;
    private long startTime;
    private boolean submitted = false;

    // ─────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────
    public QuizAttemptFrame(Quiz quiz, User student, Runnable onComplete) {
        this.quiz = quiz;
        this.student = student;
        this.onComplete = onComplete;

        QuizDAO quizDAO = new QuizDAO();
        this.questions = quizDAO.getQuestionsByQuiz(quiz.getQuizId());
        this.totalSeconds = quiz.getTimeLimitMinutes() * 60;

        // Try connecting to socket server; fallback to direct DB mode
        tryConnectSocket();
        initUI();
        startTimer();
    }

    private void tryConnectSocket() {
        quizClient = new QuizClient(DatabaseConfig.SERVER_HOST, DatabaseConfig.SERVER_PORT);
        boolean connected = quizClient.connect();
        if (connected) {
            quizClient.setStatusCallback(msg -> SwingUtilities.invokeLater(() ->
                statusLabel.setText("Server: " + msg)));
            quizClient.setResultCallback(msg -> SwingUtilities.invokeLater(() ->
                handleSocketResult(msg)));
            quizClient.joinQuiz(quiz.getQuizId(), student.getUserId(), student.getName());
            System.out.println("[CLIENT] Connected to quiz server via socket.");
        } else {
            useSocket = false;
            System.out.println("[CLIENT] Socket server unavailable — using direct DB mode.");
        }
    }

    // ─────────────────────────────────────────────────────────
    // UI BUILD
    // ─────────────────────────────────────────────────────────
    private void initUI() {
        setTitle("📝 Quiz: " + quiz.getQuizTitle());
        setSize(860, 720);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (!submitted && UITheme.showConfirm(QuizAttemptFrame.this, "Exit Quiz",
                    "You haven't submitted yet. Exit and lose progress?")) {
                    cleanupAndClose();
                }
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_DARK);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildQuestionScroll(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        add(root);
        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(16, 24, 12, 24));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setBackground(UITheme.BG_DARK);
        left.add(UITheme.createLabel(quiz.getQuizTitle(), UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY));
        progressLabel = UITheme.createLabel(questions.size() + " questions", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        left.add(progressLabel);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new GridLayout(2, 1, 0, 2));
        right.setBackground(UITheme.BG_DARK);
        right.setAlignmentX(Component.RIGHT_ALIGNMENT);

        timerLabel = new JLabel(formatTime(totalSeconds));
        timerLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        timerLabel.setForeground(UITheme.SECONDARY);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusLabel = UITheme.createLabel(useSocket ? "🟢 Connected to server" : "💾 Direct mode", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        right.add(timerLabel);
        right.add(statusLabel);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane buildQuestionScroll() {
        questionContainer = new JPanel();
        questionContainer.setLayout(new BoxLayout(questionContainer, BoxLayout.Y_AXIS));
        questionContainer.setBackground(UITheme.BG_CONTENT);
        questionContainer.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));

        questionPanels = new JPanel[questions.size()];
        optionGroups = new ButtonGroup[questions.size()];
        optionButtons = new JRadioButton[questions.size()][4];

        for (int i = 0; i < questions.size(); i++) {
            questionPanels[i] = buildQuestionPanel(i, questions.get(i));
            questionContainer.add(questionPanels[i]);
            questionContainer.add(Box.createVerticalStrut(16));
        }

        JScrollPane sp = UITheme.createScrollPane(questionContainer);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        return sp;
    }

    private JPanel buildQuestionPanel(int index, Question q) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Question number + text
        JPanel qHeader = new JPanel(new BorderLayout());
        qHeader.setBackground(UITheme.BG_CARD);

        JLabel numLabel = new JLabel("Q" + (index + 1));
        numLabel.setFont(UITheme.FONT_SMALL);
        numLabel.setForeground(UITheme.PRIMARY_LIGHT);
        numLabel.setBackground(new Color(67, 97, 238, 40));
        numLabel.setOpaque(true);
        numLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JTextArea qText = new JTextArea(q.getQuestionText());
        qText.setFont(UITheme.FONT_SUBHEADING);
        qText.setForeground(UITheme.TEXT_PRIMARY);
        qText.setBackground(UITheme.BG_CARD);
        qText.setEditable(false);
        qText.setLineWrap(true);
        qText.setWrapStyleWord(true);
        qText.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        qHeader.add(numLabel, BorderLayout.NORTH);
        qHeader.add(qText, BorderLayout.CENTER);
        card.add(qHeader, BorderLayout.NORTH);

        // Options
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 6));
        optionsPanel.setBackground(UITheme.BG_CARD);

        optionGroups[index] = new ButtonGroup();
        String[] opts = q.getOptionsArray();
        char[] labels = {'A', 'B', 'C', 'D'};

        for (int j = 0; j < 4; j++) {
            JPanel optRow = buildOptionRow(labels[j], opts[j], index, j);
            optionsPanel.add(optRow);
            optionButtons[index][j] = (JRadioButton) ((JPanel) optRow.getComponent(0)).getComponent(0);
            optionGroups[index].add(optionButtons[index][j]);
        }
        card.add(optionsPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOptionRow(char label, String text, int qIdx, int optIdx) {
        JPanel row = new JPanel(new BorderLayout());
        Color rowBg = UITheme.BG_CONTENT;
        row.setBackground(rowBg);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        inner.setBackground(rowBg);

        JRadioButton radio = new JRadioButton();
        radio.setBackground(rowBg);
        radio.setFocusPainted(false);

        JLabel labelLbl = new JLabel(String.valueOf(label));
        labelLbl.setFont(UITheme.FONT_BUTTON);
        labelLbl.setForeground(UITheme.PRIMARY_LIGHT);
        labelLbl.setPreferredSize(new Dimension(20, 20));

        JLabel textLbl = new JLabel(text);
        textLbl.setFont(UITheme.FONT_BODY);
        textLbl.setForeground(UITheme.TEXT_PRIMARY);

        inner.add(radio);
        inner.add(labelLbl);
        inner.add(textLbl);
        row.add(inner, BorderLayout.CENTER);

        // Highlight on selection
        radio.addActionListener(e -> {
            for (int j = 0; j < 4; j++) {
                JRadioButton rb = optionButtons[qIdx][j];
                Component parent = rb.getParent().getParent();
                parent.setBackground(UITheme.BG_CONTENT);
                rb.getParent().setBackground(UITheme.BG_CONTENT);
            }
            row.setBackground(new Color(67, 97, 238, 60));
            inner.setBackground(new Color(67, 97, 238, 60));
        });
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { radio.doClick(); }
        });

        return row;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_DARK);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 24, 16, 24));

        JLabel hint = UITheme.createLabel("Answer all questions before submitting.", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        bar.add(hint, BorderLayout.WEST);

        submitBtn = UITheme.createSuccessButton("Submit Quiz ✓");
        submitBtn.setPreferredSize(new Dimension(160, 44));
        submitBtn.addActionListener(e -> {
            if (!submitted) confirmAndSubmit();
        });
        bar.add(submitBtn, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────
    // TIMER
    // ─────────────────────────────────────────────────────────
    private void startTimer() {
        startTime = System.currentTimeMillis();
        final int[] remaining = {totalSeconds};
        countdownTimer = new Timer(1000, e -> {
            remaining[0]--;
            SwingUtilities.invokeLater(() -> {
                timerLabel.setText(formatTime(remaining[0]));
                if (remaining[0] <= 60) timerLabel.setForeground(UITheme.WARNING);
                if (remaining[0] <= 10) timerLabel.setForeground(UITheme.DANGER);
                if (remaining[0] <= 0) {
                    ((Timer) e.getSource()).stop();
                    if (!submitted) {
                        UITheme.showInfo(QuizAttemptFrame.this, "Time's Up!", "Time is up! Auto-submitting...");
                        doSubmit();
                    }
                }
            });
        });
        countdownTimer.start();
    }

    private String formatTime(int secs) {
        int m = secs / 60, s = secs % 60;
        return String.format("%02d:%02d", m, s);
    }

    // ─────────────────────────────────────────────────────────
    // SUBMIT
    // ─────────────────────────────────────────────────────────
    private void confirmAndSubmit() {
        int unanswered = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (optionGroups[i].getSelection() == null) unanswered++;
        }
        String msg = unanswered > 0
            ? unanswered + " question(s) unanswered. Submit anyway?"
            : "Submit your quiz now?";
        if (UITheme.showConfirm(this, "Submit Quiz", msg)) doSubmit();
    }

    private void doSubmit() {
        if (submitted) return;
        submitted = true;
        if (countdownTimer != null) countdownTimer.stop();
        submitBtn.setEnabled(false);

        int timeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);

        // Collect answers (1-based, 0 = unanswered)
        int[] answers = new int[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            ButtonModel sel = optionGroups[i].getSelection();
            if (sel != null) {
                for (int j = 0; j < 4; j++) {
                    if (optionButtons[i][j].getModel() == sel) {
                        answers[i] = j + 1;
                        break;
                    }
                }
            }
        }

        if (useSocket && quizClient != null && quizClient.isConnected()) {
            // Submit via socket — result handled by callback
            quizClient.submitAnswers(student.getUserId(), quiz.getQuizId(), timeTaken, answers);
        } else {
            // Direct DB submission
            int score = 0;
            for (int i = 0; i < questions.size(); i++) {
                if (answers[i] == questions.get(i).getCorrectAnswer()) score++;
            }
            ResultDAO resultDAO = new ResultDAO();
            int resultId = resultDAO.saveResult(student.getUserId(), quiz.getQuizId(), score, questions.size(), timeTaken);
            if (resultId > 0) {
                for (int i = 0; i < questions.size(); i++) {
                    boolean correct = answers[i] == questions.get(i).getCorrectAnswer();
                    resultDAO.saveAnswerDetail(resultId, questions.get(i).getQuestionId(), answers[i], correct);
                }
            }
            showResult(score, questions.size(), timeTaken);
        }
    }

    private void handleSocketResult(String resultData) {
        // Format: score:total:percentage
        String[] parts = resultData.split(":");
        if (parts.length >= 2) {
            int score = Integer.parseInt(parts[0]);
            int total = Integer.parseInt(parts[1]);
            int timeTaken = (int) ((System.currentTimeMillis() - startTime) / 1000);
            showResult(score, total, timeTaken);
        }
    }

    private void showResult(int score, int total, int timeTaken) {
        SwingUtilities.invokeLater(() -> {
            if (quizClient != null) quizClient.disconnect();

            double pct = total > 0 ? (double) score / total * 100 : 0;
            String grade = pct >= 90 ? "Excellent! 🏆" : pct >= 70 ? "Good Job! 👍" : pct >= 50 ? "Keep Practicing 📚" : "Needs Improvement 💪";

            JDialog result = new JDialog(this, "Quiz Result", true);
            result.setSize(440, 360);
            result.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(UITheme.BG_CARD);
            panel.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(8, 0, 8, 0); g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

            g.gridy = 0; panel.add(UITheme.createLabel("Quiz Completed!", UITheme.FONT_TITLE, UITheme.TEXT_PRIMARY), g);
            g.gridy = 1;
            JLabel scoreLabel = new JLabel(score + " / " + total);
            scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
            scoreLabel.setForeground(pct >= 70 ? UITheme.SECONDARY : UITheme.WARNING);
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(scoreLabel, g);
            g.gridy = 2; panel.add(UITheme.createLabel(String.format("%.1f%% — %s", pct, grade), UITheme.FONT_HEADING, UITheme.TEXT_SECONDARY), g);
            g.gridy = 3; panel.add(UITheme.createLabel("Time taken: " + formatTime(timeTaken), UITheme.FONT_SMALL, UITheme.TEXT_MUTED), g);
            g.gridy = 4; g.insets = new Insets(20, 0, 0, 0);
            JButton closeBtn = UITheme.createPrimaryButton("Done");
            panel.add(closeBtn, g);

            closeBtn.addActionListener(e -> { result.dispose(); cleanupAndClose(); });
            result.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) { cleanupAndClose(); }
            });
            result.add(panel);
            result.setVisible(true);
        });
    }

    private void cleanupAndClose() {
        if (countdownTimer != null) countdownTimer.stop();
        if (quizClient != null) quizClient.disconnect();
        dispose();
        if (onComplete != null) onComplete.run();
    }
}
