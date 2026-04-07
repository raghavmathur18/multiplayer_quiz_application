package com.quizapp.models;

import java.io.Serializable;

public class AnswerDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int resultId;
    private int questionId;
    private int selectedAnswer;
    private boolean isCorrect;

    public AnswerDetail() {}

    public AnswerDetail(int resultId, int questionId, int selectedAnswer, boolean isCorrect) {
        this.resultId = resultId;
        this.questionId = questionId;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public int getSelectedAnswer() { return selectedAnswer; }
    public void setSelectedAnswer(int selectedAnswer) { this.selectedAnswer = selectedAnswer; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}
