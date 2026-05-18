package com.quizapp.server;

import com.quizapp.database.QuizDAO;
import com.quizapp.database.ResultDAO;
import com.quizapp.models.Question;
import com.quizapp.models.Quiz;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Handles individual student socket connections.
 * Protocol (text-based, newline-delimited):
 *   Client -> Server: JOIN:<quizId>:<studentId>:<studentName>
 *   Server -> Client: QUIZ_INFO:<title>:<questionCount>:<timeLimitSec>
 *   Server -> Client: QUESTION:<index>:<total>:<text>|<opt1>|<opt2>|<opt3>|<opt4>
 *   Client -> Server: ANSWER:<questionIndex>:<answer(1-4)>
 *   Client -> Server: SUBMIT:<studentId>:<quizId>:<timeTaken>:<ans1,ans2,...>
 *   Server -> Client: RESULT:<score>:<total>:<percentage>
 *   Server -> Client: ERROR:<message>
 *   Server -> Client: STATUS:<message>
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private QuizServer server;
    private PrintWriter out;
    private BufferedReader in;
    private int studentId = -1;
    private String studentName = "";
    private int currentQuizId = -1;

    public ClientHandler(Socket socket, QuizServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line.trim());
            }
        } catch (IOException e) {
            System.out.println("[CLIENT] Connection closed: " + studentName);
        } finally {
            cleanup();
        }
    }

    private void handleMessage(String message) {
        System.out.println("[SERVER] Received: " + message);
        String[] parts = message.split(":", 5);
        String command = parts[0];

        switch (command) {
            case "JOIN" -> handleJoin(parts);
            case "SUBMIT" -> handleSubmit(parts);
            case "PING" -> send("PONG");
            case "STATUS_CHECK" -> handleStatusCheck(parts);
            default -> send("ERROR:Unknown command: " + command);
        }
    }

    private void handleJoin(String[] parts) {
        if (parts.length < 4) { send("ERROR:Invalid JOIN format"); return; }
        try {
            int quizId = Integer.parseInt(parts[1]);
            studentId = Integer.parseInt(parts[2]);
            studentName = parts[3];
            currentQuizId = quizId;

            QuizSession session = server.getOrCreateSession(quizId);
            if (session == null) { send("ERROR:Quiz not found or not active"); return; }

            session.addStudent(studentId, studentName);
            Quiz quiz = session.getQuiz();
            List<Question> questions = quiz.getQuestions();

            // Send quiz info
            send("QUIZ_INFO:" + quiz.getQuizTitle() + ":" + questions.size() + ":" + (quiz.getTimeLimitMinutes() * 60));
            send("STATUS:Joined successfully. Quiz: " + quiz.getQuizTitle());

            // Send all questions
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String qMsg = "QUESTION:" + (i + 1) + ":" + questions.size() + ":" +
                    q.getQuestionText() + "|" + q.getOption1() + "|" + q.getOption2() + "|" + q.getOption3() + "|" + q.getOption4();
                send(qMsg);
            }
            send("READY");
        } catch (NumberFormatException e) {
            send("ERROR:Invalid JOIN parameters");
        }
    }

    private void handleSubmit(String[] parts) {
        // SUBMIT:<studentId>:<quizId>:<timeTaken>:<ans1,ans2,...,ansN>
        if (parts.length < 5) { send("ERROR:Invalid SUBMIT format"); return; }
        try {
            int sId = Integer.parseInt(parts[1]);
            int qId = Integer.parseInt(parts[2]);
            int timeTaken = Integer.parseInt(parts[3]);
            String[] answerStrs = parts[4].split(",");

            QuizDAO quizDAO = new QuizDAO();
            ResultDAO resultDAO = new ResultDAO();
            Quiz quiz = quizDAO.getQuizById(qId);

            if (quiz == null) { send("ERROR:Quiz not found"); return; }

            List<Question> questions = quiz.getQuestions();
            int score = 0;
            int[] answers = new int[answerStrs.length];

            for (int i = 0; i < answerStrs.length; i++) {
                try { answers[i] = Integer.parseInt(answerStrs[i].trim()); } catch (NumberFormatException e) { answers[i] = 0; }
            }

            // Calculate score
            for (int i = 0; i < questions.size() && i < answers.length; i++) {
                if (answers[i] == questions.get(i).getCorrectAnswer()) score++;
            }

            // Save result to DB
            int resultId = resultDAO.saveResult(sId, qId, score, questions.size(), timeTaken);
            if (resultId > 0) {
                for (int i = 0; i < questions.size() && i < answers.length; i++) {
                    boolean isCorrect = answers[i] == questions.get(i).getCorrectAnswer();
                    resultDAO.saveAnswerDetail(resultId, questions.get(i).getQuestionId(), answers[i], isCorrect);
                }
            }

            // Mark student as submitted in session
            QuizSession session = server.getActiveSessions().get(qId);
            if (session != null) session.markSubmitted(sId);

            double pct = questions.size() > 0 ? (double) score / questions.size() * 100 : 0;
            send("RESULT:" + score + ":" + questions.size() + ":" + String.format("%.1f", pct));
            System.out.println("[SERVER] Result saved for student " + sId + ": " + score + "/" + questions.size());

        } catch (NumberFormatException e) {
            send("ERROR:Invalid SUBMIT parameters");
        }
    }

    private void handleStatusCheck(String[] parts) {
        if (currentQuizId < 0) { send("STATUS_RESPONSE:NOT_IN_QUIZ"); return; }
        QuizSession session = server.getActiveSessions().get(currentQuizId);
        if (session == null) { send("STATUS_RESPONSE:SESSION_ENDED"); return; }
        send("STATUS_RESPONSE:ACTIVE:" + session.getRemainingSeconds() + ":" + session.getStudentCount());
    }

    private void send(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    private void cleanup() {
        if (studentId > 0 && currentQuizId > 0) {
            QuizSession session = server.getActiveSessions().get(currentQuizId);
            if (session != null) session.removeStudent(studentId);
        }
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
