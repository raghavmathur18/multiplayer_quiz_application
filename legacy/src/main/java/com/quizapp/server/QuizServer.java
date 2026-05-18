package com.quizapp.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * QuizServer - Java Socket Programming server for real-time multiplayer quiz.
 * Handles multiple student connections simultaneously using a cached thread pool.
 * Run this before launching the GUI client applications.
 */
public class QuizServer {
    private static final int PORT = 9090;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    // Active quiz sessions: quizId -> QuizSession
    private Map<Integer, QuizSession> activeSessions = new ConcurrentHashMap<>();

    public QuizServer() {
        threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("[SERVER] ======================================");
            System.out.println("[SERVER] Quiz Competition Server Started!");
            System.out.println("[SERVER] Listening on port: " + PORT);
            System.out.println("[SERVER] ======================================");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New client connected: " + clientSocket.getInetAddress());
                    threadPool.execute(new ClientHandler(clientSocket, this));
                } catch (SocketException e) {
                    if (running) System.err.println("[SERVER] Socket error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start server: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("[SERVER] Server stopped.");
    }

    public QuizSession getOrCreateSession(int quizId) {
        return activeSessions.computeIfAbsent(quizId, id -> {
            com.quizapp.database.QuizDAO dao = new com.quizapp.database.QuizDAO();
            com.quizapp.models.Quiz quiz = dao.getQuizById(id);
            if (quiz != null) {
                QuizSession session = new QuizSession(quiz);
                System.out.println("[SERVER] Created session for quiz: " + quiz.getQuizTitle());
                return session;
            }
            return null;
        });
    }

    public void endSession(int quizId) {
        activeSessions.remove(quizId);
    }

    public Map<Integer, QuizSession> getActiveSessions() { return activeSessions; }

    public static void main(String[] args) {
        QuizServer server = new QuizServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
