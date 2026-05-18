package com.quizapp.models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resultId;
    private int studentId;
    private String studentName;
    private int quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private int timeTakenSeconds;
    private Timestamp attemptedAt;

    public Result() {}

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }
    public Timestamp getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(Timestamp attemptedAt) { this.attemptedAt = attemptedAt; }

    public double getPercentage() {
        if (totalQuestions == 0) return 0;
        return (double) score / totalQuestions * 100;
    }
}
