package com.quizapp.models;

import java.io.Serializable;

public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    private int questionId;
    private int quizId;
    private String questionText;
    private String option1, option2, option3, option4;
    private int correctAnswer; // 1-4

    public Question() {}

    public Question(int questionId, String questionText, String o1, String o2, String o3, String o4, int correct) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.option1 = o1; this.option2 = o2;
        this.option3 = o3; this.option4 = o4;
        this.correctAnswer = correct;
    }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }
    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }
    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }
    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }
    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String[] getOptionsArray() {
        return new String[]{option1, option2, option3, option4};
    }

    public String getOptionByIndex(int index) {
        return switch (index) {
            case 1 -> option1;
            case 2 -> option2;
            case 3 -> option3;
            case 4 -> option4;
            default -> "";
        };
    }
}
