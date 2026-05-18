package com.quizapp.gui.teacher;

import com.quizapp.database.QuizDAO;
import com.quizapp.gui.common.UITheme;
import com.quizapp.models.Question;
import com.quizapp.models.Quiz;
import com.quizapp.models.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * QuizEditorFrame — allows teacher to add, view, and delete questions for a quiz.
 */
public class QuizEditorFrame extends JFrame {
    private Quiz quiz;
    private User teacher;
    private Runnable onClose;
    private QuizDAO quizDAO = new QuizDAO();

    private JPanel questionListPanel;

    public QuizEditorFrame(Quiz quiz, User teacher, Runnable onClose) {
        this.quiz = quiz;
        this.teacher = teacher;
        this.onClose = onClose;
        initUI();
    }

    private void initUI() {
        setTitle("Quiz Editor — " + quiz.getQuizTitle());
        setSize(900, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { if (onClose != null) onClose.run(); }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_CONTENT);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.BG_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        headerLeft.setBackground(UITheme.BG_DARK);
        headerLeft.add(UITheme.createLabel(quiz.getQuizTitle(), UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY));
        headerLeft.add(UITheme.createLabel(
            "Class ID: " + quiz.getClassId() + "  |  Time Limit: " + quiz.getTimeLimitMinutes() + " min",
            UITheme.FONT_SMALL, UITheme.TEXT_MUTED));
        header.add(headerLeft, BorderLayout.WEST);

        JButton addBtn = UITheme.createSuccessButton("+ Add Question");
        addBtn.addActionListener(e -> showAddQuestionDialog());
        header.add(addBtn, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Split: question list (left) | add form (right)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(380);
        split.setDividerSize(2);

        // Left: question list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UITheme.BG_CONTENT);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 10));
        JLabel qListTitle = UITheme.createLabel("Questions", UITheme.FONT_SUBHEADING, UITheme.TEXT_SECONDARY);
        leftPanel.add(qListTitle, BorderLayout.NORTH);

        questionListPanel = new JPanel();
        questionListPanel.setLayout(new BoxLayout(questionListPanel, BoxLayout.Y_AXIS));
        questionListPanel.setBackground(UITheme.BG_CONTENT);
        leftPanel.add(UITheme.createScrollPane(questionListPanel), BorderLayout.CENTER);
        split.setLeftComponent(leftPanel);

        // Right: blank / prompt
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UITheme.BG_CONTENT);
        rightPanel.add(UITheme.createLabel("Click '+ Add Question' to add a question.", UITheme.FONT_BODY, UITheme.TEXT_MUTED));
        split.setRightComponent(rightPanel);

        root.add(split, BorderLayout.CENTER);
        add(root);
        loadQuestions();
        setVisible(true);
    }

    private void loadQuestions() {
        questionListPanel.removeAll();
        List<Question> questions = quizDAO.getQuestionsByQuiz(quiz.getQuizId());

        if (questions.isEmpty()) {
            JLabel empty = UITheme.createLabel("No questions yet.", UITheme.FONT_BODY, UITheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            questionListPanel.add(Box.createVerticalStrut(20));
            questionListPanel.add(empty);
        }

        for (int i = 0; i < questions.size(); i++) {
            questionListPanel.add(buildQuestionRow(i + 1, questions.get(i)));
            questionListPanel.add(Box.createVerticalStrut(8));
        }
        questionListPanel.revalidate();
        questionListPanel.repaint();
    }

    private JPanel buildQuestionRow(int num, Question q) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UITheme.BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setBackground(UITheme.BG_CARD);

        String shortText = q.getQuestionText().length() > 60
            ? q.getQuestionText().substring(0, 57) + "..."
            : q.getQuestionText();
        JLabel qText = UITheme.createLabel("Q" + num + ". " + shortText, UITheme.FONT_BODY, UITheme.TEXT_PRIMARY);
        JLabel correct = UITheme.createLabel("✓ Answer: Option " + q.getCorrectAnswer(), UITheme.FONT_SMALL, UITheme.SECONDARY);
        textPanel.add(qText);
        textPanel.add(correct);
        row.add(textPanel, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("✕");
        deleteBtn.setFont(UITheme.FONT_SMALL);
        deleteBtn.setForeground(UITheme.DANGER);
        deleteBtn.setBackground(UITheme.BG_CARD);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.addActionListener(e -> {
            if (UITheme.showConfirm(this, "Delete Question", "Delete this question?")) {
                quizDAO.deleteQuestion(q.getQuestionId());
                loadQuestions();
            }
        });
        row.add(deleteBtn, BorderLayout.EAST);
        return row;
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog(this, "Add Question", true);
        dialog.setSize(580, 560);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 0, 5, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        g.gridy = 0;
        panel.add(UITheme.createLabel("Add New Question", UITheme.FONT_HEADING, UITheme.TEXT_PRIMARY), g);

        g.gridy = 1;
        panel.add(UITheme.createLabel("Question Text *", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 2;
        g.weighty = 0.3;
        g.fill = GridBagConstraints.BOTH;
        JTextArea questionText = UITheme.createTextArea(3, 40);
        panel.add(UITheme.createScrollPane(questionText), g);
        g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;

        String[] optionLabels = {"Option A *", "Option B *", "Option C *", "Option D *"};
        JTextField[] optionFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            g.gridy = 3 + (i * 2);
            panel.add(UITheme.createLabel(optionLabels[i], UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
            g.gridy = 4 + (i * 2);
            optionFields[i] = UITheme.createTextField("Enter option " + (char)('A' + i));
            panel.add(optionFields[i], g);
        }

        g.gridy = 11;
        panel.add(UITheme.createLabel("Correct Answer *", UITheme.FONT_SMALL, UITheme.TEXT_SECONDARY), g);
        g.gridy = 12;
        JComboBox<String> correctCombo = UITheme.createComboBox(new String[]{"Option A (1)", "Option B (2)", "Option C (3)", "Option D (4)"});
        panel.add(correctCombo, g);

        g.gridy = 13;
        g.insets = new Insets(18, 0, 0, 0);
        JButton saveBtn = UITheme.createPrimaryButton("Save Question");
        panel.add(saveBtn, g);

        saveBtn.addActionListener(e -> {
            String qText = questionText.getText().trim();
            String o1 = optionFields[0].getText().trim();
            String o2 = optionFields[1].getText().trim();
            String o3 = optionFields[2].getText().trim();
            String o4 = optionFields[3].getText().trim();
            int correct = correctCombo.getSelectedIndex() + 1;

            if (qText.isEmpty() || o1.isEmpty() || o2.isEmpty() || o3.isEmpty() || o4.isEmpty()) {
                UITheme.showError(dialog, "Validation", "All fields are required.");
                return;
            }
            boolean saved = quizDAO.addQuestion(quiz.getQuizId(), qText, o1, o2, o3, o4, correct);
            if (saved) {
                UITheme.showInfo(dialog, "Saved", "Question added successfully!");
                dialog.dispose();
                loadQuestions();
            } else {
                UITheme.showError(dialog, "Error", "Failed to save question.");
            }
        });

        dialog.add(UITheme.createScrollPane(panel));
        dialog.setVisible(true);
    }
}
