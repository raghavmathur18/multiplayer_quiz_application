package com.quizapp.database;

import com.quizapp.models.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {
    private DatabaseManager dbManager;

    public ResultDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int saveResult(int studentId, int quizId, int score, int totalQuestions, int timeTakenSeconds) {
        String sql = "INSERT INTO results (student_id, quiz_id, score, total_questions, time_taken_seconds) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId); ps.setInt(2, quizId); ps.setInt(3, score);
            ps.setInt(4, totalQuestions); ps.setInt(5, timeTakenSeconds);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("Save result error: " + e.getMessage()); }
        return -1;
    }

    public boolean saveAnswerDetail(int resultId, int questionId, int selectedAnswer, boolean isCorrect) {
        String sql = "INSERT INTO answer_details (result_id, question_id, selected_answer, is_correct) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, resultId); ps.setInt(2, questionId);
            ps.setInt(3, selectedAnswer); ps.setBoolean(4, isCorrect);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Save answer detail error: " + e.getMessage()); }
        return false;
    }

    public List<Result> getStudentResults(int studentId) {
        List<Result> results = new ArrayList<Result>();
        String sql = "SELECT r.*, q.quiz_title, u.name as student_name FROM results r JOIN quizzes q ON r.quiz_id = q.quiz_id JOIN users u ON r.student_id = u.user_id WHERE r.student_id = ? ORDER BY r.attempted_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapResult(rs));
        } catch (SQLException e) { System.err.println("Get student results error: " + e.getMessage()); }
        return results;
    }

    public List<Result> getQuizResults(int quizId) {
        List<Result> results = new ArrayList<Result>();
        String sql = "SELECT r.*, q.quiz_title, u.name as student_name FROM results r JOIN quizzes q ON r.quiz_id = q.quiz_id JOIN users u ON r.student_id = u.user_id WHERE r.quiz_id = ? ORDER BY r.score DESC, r.time_taken_seconds ASC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapResult(rs));
        } catch (SQLException e) { System.err.println("Get quiz results error: " + e.getMessage()); }
        return results;
    }

    public List<Result> getClassResults(int classId) {
        List<Result> results = new ArrayList<Result>();
        String sql = "SELECT r.*, q.quiz_title, u.name as student_name FROM results r JOIN quizzes q ON r.quiz_id = q.quiz_id JOIN users u ON r.student_id = u.user_id WHERE q.class_id = ? ORDER BY r.attempted_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapResult(rs));
        } catch (SQLException e) { System.err.println("Get class results error: " + e.getMessage()); }
        return results;
    }

    public boolean hasAttemptedQuiz(int studentId, int quizId) {
        String sql = "SELECT COUNT(*) FROM results WHERE student_id = ? AND quiz_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.err.println("Has attempted error: " + e.getMessage()); }
        return false;
    }

    public int[] getCorrectIncorrectCount(int quizId) {
        String sql = "SELECT SUM(CASE WHEN ad.is_correct = TRUE THEN 1 ELSE 0 END) as correct_count, SUM(CASE WHEN ad.is_correct = FALSE THEN 1 ELSE 0 END) as incorrect_count FROM answer_details ad JOIN results r ON ad.result_id = r.result_id WHERE r.quiz_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("correct_count"), rs.getInt("incorrect_count")};
        } catch (SQLException e) { System.err.println("Get correct/incorrect error: " + e.getMessage()); }
        return new int[]{0, 0};
    }

    public int[] getStudentCorrectIncorrect(int studentId) {
        String sql = "SELECT SUM(CASE WHEN ad.is_correct = TRUE THEN 1 ELSE 0 END) as correct_count, SUM(CASE WHEN ad.is_correct = FALSE THEN 1 ELSE 0 END) as incorrect_count FROM answer_details ad JOIN results r ON ad.result_id = r.result_id WHERE r.student_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("correct_count"), rs.getInt("incorrect_count")};
        } catch (SQLException e) { System.err.println("Get student correct/incorrect error: " + e.getMessage()); }
        return new int[]{0, 0};
    }

    private Result mapResult(ResultSet rs) throws SQLException {
        Result result = new Result();
        result.setResultId(rs.getInt("result_id"));
        result.setStudentId(rs.getInt("student_id"));
        result.setStudentName(rs.getString("student_name"));
        result.setQuizId(rs.getInt("quiz_id"));
        result.setQuizTitle(rs.getString("quiz_title"));
        result.setScore(rs.getInt("score"));
        result.setTotalQuestions(rs.getInt("total_questions"));
        result.setTimeTakenSeconds(rs.getInt("time_taken_seconds"));
        result.setAttemptedAt(rs.getTimestamp("attempted_at"));
        return result;
    }
}
