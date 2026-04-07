package com.quizapp.client;

import com.quizapp.models.Question;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * QuizClient - connects to QuizServer via Java Sockets.
 * Used by the student GUI to participate in live quiz sessions.
 */
public class QuizClient {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean connected = false;

    // Callbacks for async messages
    private Consumer<String> statusCallback;
    private Consumer<Question> questionCallback;
    private Consumer<String> resultCallback;
    private Consumer<String> quizInfoCallback;
    private Runnable readyCallback;
    private Runnable disconnectCallback;

    // Received questions
    private List<Question> receivedQuestions = new ArrayList<>();
    private String quizTitle = "";
    private int totalQuestions = 0;
    private int timeLimitSeconds = 0;

    public QuizClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            // Start listener thread
            Thread listener = new Thread(this::listenForMessages);
            listener.setDaemon(true);
            listener.start();

            return true;
        } catch (IOException e) {
            System.err.println("[CLIENT] Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                handleServerMessage(line.trim());
            }
        } catch (IOException e) {
            if (connected) System.out.println("[CLIENT] Disconnected from server.");
        } finally {
            connected = false;
            if (disconnectCallback != null) disconnectCallback.run();
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("[CLIENT] Received: " + message);

        if (message.startsWith("QUIZ_INFO:")) {
            // QUIZ_INFO:<title>:<questionCount>:<timeLimitSec>
            String[] parts = message.split(":", 4);
            if (parts.length >= 4) {
                quizTitle = parts[1];
                totalQuestions = Integer.parseInt(parts[2]);
                timeLimitSeconds = Integer.parseInt(parts[3]);
                if (quizInfoCallback != null) quizInfoCallback.accept(quizTitle + "|" + totalQuestions + "|" + timeLimitSeconds);
            }
        } else if (message.startsWith("QUESTION:")) {
            // QUESTION:<index>:<total>:<text>|<opt1>|<opt2>|<opt3>|<opt4>
            String[] parts = message.split(":", 4);
            if (parts.length == 4) {
                int index = Integer.parseInt(parts[1]);
                String[] qParts = parts[3].split("\\|", 5);
                if (qParts.length == 5) {
                    Question q = new Question();
                    q.setQuestionId(index);
                    q.setQuestionText(qParts[0]);
                    q.setOption1(qParts[1]);
                    q.setOption2(qParts[2]);
                    q.setOption3(qParts[3]);
                    q.setOption4(qParts[4]);
                    receivedQuestions.add(q);
                    if (questionCallback != null) questionCallback.accept(q);
                }
            }
        } else if (message.startsWith("READY")) {
            if (readyCallback != null) readyCallback.run();
        } else if (message.startsWith("RESULT:")) {
            if (resultCallback != null) resultCallback.accept(message.substring(7));
        } else if (message.startsWith("STATUS:")) {
            if (statusCallback != null) statusCallback.accept(message.substring(7));
        } else if (message.startsWith("ERROR:")) {
            if (statusCallback != null) statusCallback.accept("ERROR: " + message.substring(6));
        }
    }

    public void joinQuiz(int quizId, int studentId, String studentName) {
        sendMessage("JOIN:" + quizId + ":" + studentId + ":" + studentName);
    }

    public void submitAnswers(int studentId, int quizId, int timeTaken, int[] answers) {
        StringBuilder sb = new StringBuilder("SUBMIT:");
        sb.append(studentId).append(":").append(quizId).append(":").append(timeTaken).append(":");
        for (int i = 0; i < answers.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(answers[i]);
        }
        sendMessage(sb.toString());
    }

    private void sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() { return connected; }
    public List<Question> getReceivedQuestions() { return receivedQuestions; }
    public String getQuizTitle() { return quizTitle; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }

    // Callback setters
    public void setStatusCallback(Consumer<String> cb) { this.statusCallback = cb; }
    public void setQuestionCallback(Consumer<Question> cb) { this.questionCallback = cb; }
    public void setResultCallback(Consumer<String> cb) { this.resultCallback = cb; }
    public void setQuizInfoCallback(Consumer<String> cb) { this.quizInfoCallback = cb; }
    public void setReadyCallback(Runnable cb) { this.readyCallback = cb; }
    public void setDisconnectCallback(Runnable cb) { this.disconnectCallback = cb; }
}
