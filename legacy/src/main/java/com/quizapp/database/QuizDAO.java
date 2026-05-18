package com.quizapp.database;

import com.quizapp.models.Question;
import com.quizapp.models.Quiz;
import java.sql.*;
import java.util.*;

public class QuizDAO {
    private DatabaseManager dbManager;

    public QuizDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public Quiz createQuiz(String title, int classId, int timeLimitMinutes) {
        String sql = "INSERT INTO quizzes (quiz_title, class_id, time_limit_minutes) VALUES (?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setInt(2, classId);
            ps.setInt(3, timeLimitMinutes);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                Quiz quiz = new Quiz();
                quiz.setQuizId(rs.getInt(1));
                quiz.setQuizTitle(title);
                quiz.setClassId(classId);
                quiz.setTimeLimitMinutes(timeLimitMinutes);
                return quiz;
            }
        } catch (SQLException e) {
            System.err.println("Create quiz error: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteQuiz(int quizId) {
        String sql = "DELETE FROM quizzes WHERE quiz_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete quiz error: " + e.getMessage());
        }
        return false;
    }

    public boolean addQuestion(int quizId, String questionText, String o1, String o2, String o3, String o4, int correct) {
        String sql = "INSERT INTO questions (quiz_id, question_text, option1, option2, option3, option4, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setString(2, questionText);
            ps.setString(3, o1); ps.setString(4, o2);
            ps.setString(5, o3); ps.setString(6, o4);
            ps.setInt(7, correct);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Add question error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteQuestion(int questionId) {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete question error: " + e.getMessage());
        }
        return false;
    }

    public List<Quiz> getQuizzesByClass(int classId) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT q.*, c.class_name FROM quizzes q JOIN classes c ON q.class_id = c.class_id WHERE q.class_id = ? ORDER BY q.created_at DESC";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quizzes.add(mapQuiz(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get quizzes error: " + e.getMessage());
        }
        return quizzes;
    }

    public List<Quiz> getAvailableQuizzesForStudent(int studentId, int classId) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = """
            SELECT q.*, c.class_name FROM quizzes q
            JOIN classes c ON q.class_id = c.class_id
            WHERE q.class_id = ? AND q.is_active = TRUE
            AND q.quiz_id NOT IN (
                SELECT quiz_id FROM results WHERE student_id = ?
            )
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quizzes.add(mapQuiz(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get available quizzes error: " + e.getMessage());
        }
        return quizzes;
    }

    public List<Quiz> getAllActiveQuizzesForStudent(int studentId, int classId) {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = """
            SELECT q.*, c.class_name FROM quizzes q
            JOIN classes c ON q.class_id = c.class_id
            WHERE q.class_id = ? AND q.is_active = TRUE
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quizzes.add(mapQuiz(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get all active quizzes error: " + e.getMessage());
        }
        return quizzes;
    }

    public Quiz getQuizById(int quizId) {
        String sql = "SELECT q.*, c.class_name FROM quizzes q JOIN classes c ON q.class_id = c.class_id WHERE q.quiz_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Quiz quiz = mapQuiz(rs);
                quiz.setQuestions(getQuestionsByQuiz(quizId));
                return quiz;
            }
        } catch (SQLException e) {
            System.err.println("Get quiz error: " + e.getMessage());
        }
        return null;
    }

    public List<Question> getQuestionsByQuiz(int quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY question_id";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questions.add(mapQuestion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get questions error: " + e.getMessage());
        }
        return questions;
    }

    public boolean setQuizActive(int quizId, boolean active) {
        String sql = "UPDATE quizzes SET is_active = ? WHERE quiz_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, quizId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Set quiz active error: " + e.getMessage());
        }
        return false;
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setQuizId(rs.getInt("quiz_id"));
        quiz.setQuizTitle(rs.getString("quiz_title"));
        quiz.setClassId(rs.getInt("class_id"));
        quiz.setClassName(rs.getString("class_name"));
        quiz.setTimeLimitMinutes(rs.getInt("time_limit_minutes"));
        quiz.setActive(rs.getBoolean("is_active"));
        quiz.setCreatedAt(rs.getTimestamp("created_at"));
        return quiz;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOption1(rs.getString("option1"));
        q.setOption2(rs.getString("option2"));
        q.setOption3(rs.getString("option3"));
        q.setOption4(rs.getString("option4"));
        q.setCorrectAnswer(rs.getInt("correct_answer"));
        return q;
    }
}
