package com.quizapp.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Serializable {
    private static final long serialVersionUID = 1L;

    private int quizId;
    private int classId;
    private String className;
    private String quizTitle;
    private int timeLimitMinutes;
    private boolean isActive;
    private Timestamp createdAt;
    private List<Question> questions = new ArrayList<>();

    public Quiz() {}

    public Quiz(int quizId, String quizTitle, int classId, int timeLimitMinutes) {
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.classId = classId;
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    @Override
    public String toString() { return quizTitle + (isActive ? " 🔴 LIVE" : ""); }
}
