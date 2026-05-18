package com.quizapp.server;

import com.quizapp.models.Quiz;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a live quiz session running on the server.
 * Tracks which students have joined and their submission status.
 */
public class QuizSession {
    private Quiz quiz;
    private long startTime;
    private boolean started;
    private Map<Integer, String> connectedStudents = new ConcurrentHashMap<>(); // studentId -> name
    private Map<Integer, Boolean> submissionStatus = new ConcurrentHashMap<>(); // studentId -> submitted

    public QuizSession(Quiz quiz) {
        this.quiz = quiz;
        this.started = false;
    }

    public void start() {
        this.started = true;
        this.startTime = System.currentTimeMillis();
    }

    public void addStudent(int studentId, String studentName) {
        connectedStudents.put(studentId, studentName);
        submissionStatus.put(studentId, false);
        System.out.println("[SESSION] Student joined: " + studentName + " (Quiz: " + quiz.getQuizTitle() + ")");
    }

    public void removeStudent(int studentId) {
        connectedStudents.remove(studentId);
        submissionStatus.remove(studentId);
    }

    public void markSubmitted(int studentId) {
        submissionStatus.put(studentId, true);
        System.out.println("[SESSION] Student submitted quiz. ID: " + studentId);
    }

    public boolean isStarted() { return started; }
    public Quiz getQuiz() { return quiz; }
    public long getStartTime() { return startTime; }
    public Map<Integer, String> getConnectedStudents() { return connectedStudents; }
    public int getStudentCount() { return connectedStudents.size(); }
    public boolean hasSubmitted(int studentId) { return submissionStatus.getOrDefault(studentId, false); }

    public int getRemainingSeconds() {
        if (!started) return quiz.getTimeLimitMinutes() * 60;
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        int remaining = quiz.getTimeLimitMinutes() * 60 - (int) elapsed;
        return Math.max(0, remaining);
    }

    public boolean isExpired() {
        return started && getRemainingSeconds() <= 0;
    }
}
