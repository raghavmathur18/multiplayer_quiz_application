package com.quizapp.database;

import com.quizapp.models.ClassRoom;
import com.quizapp.models.User;
import java.sql.*;
import java.util.*;

public class ClassDAO {
    private DatabaseManager dbManager;

    public ClassDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    private String generateClassCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public ClassRoom createClass(String className, int teacherId, String description) {
        String code = generateClassCode();
        String sql = "INSERT INTO classes (class_name, teacher_id, class_code, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, className);
            ps.setInt(2, teacherId);
            ps.setString(3, code);
            ps.setString(4, description);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ClassRoom cr = new ClassRoom();
                cr.setClassId(rs.getInt(1));
                cr.setClassName(className);
                cr.setTeacherId(teacherId);
                cr.setClassCode(code);
                cr.setDescription(description);
                return cr;
            }
        } catch (SQLException e) {
            System.err.println("Create class error: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteClass(int classId) {
        String sql = "DELETE FROM classes WHERE class_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete class error: " + e.getMessage());
        }
        return false;
    }

    public List<ClassRoom> getTeacherClasses(int teacherId) {
        List<ClassRoom> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, u.name as teacher_name,
                   (SELECT COUNT(*) FROM student_class sc WHERE sc.class_id = c.class_id) as student_count
            FROM classes c
            JOIN users u ON c.teacher_id = u.user_id
            WHERE c.teacher_id = ?
            ORDER BY c.created_at DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                classes.add(mapClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get teacher classes error: " + e.getMessage());
        }
        return classes;
    }

    public List<ClassRoom> getStudentClasses(int studentId) {
        List<ClassRoom> classes = new ArrayList<>();
        String sql = """
            SELECT c.*, u.name as teacher_name
            FROM classes c
            JOIN users u ON c.teacher_id = u.user_id
            JOIN student_class sc ON c.class_id = sc.class_id
            WHERE sc.student_id = ?
            ORDER BY sc.joined_at DESC
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                classes.add(mapClass(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get student classes error: " + e.getMessage());
        }
        return classes;
    }

    public ClassRoom getClassByCode(String code) {
        String sql = "SELECT c.*, u.name as teacher_name FROM classes c JOIN users u ON c.teacher_id = u.user_id WHERE c.class_code = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapClass(rs);
        } catch (SQLException e) {
            System.err.println("Get class by code error: " + e.getMessage());
        }
        return null;
    }

    public boolean joinClass(int studentId, int classId) {
        String sql = "INSERT IGNORE INTO student_class (student_id, class_id) VALUES (?, ?)";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Join class error: " + e.getMessage());
        }
        return false;
    }

    public boolean isStudentEnrolled(int studentId, int classId) {
        String sql = "SELECT COUNT(*) FROM student_class WHERE student_id = ? AND class_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Enrollment check error: " + e.getMessage());
        }
        return false;
    }

    public List<User> getClassStudents(int classId) {
        List<User> students = new ArrayList<>();
        String sql = """
            SELECT u.* FROM users u
            JOIN student_class sc ON u.user_id = sc.student_id
            WHERE sc.class_id = ?
            ORDER BY u.name
        """;
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, classId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                students.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Get class students error: " + e.getMessage());
        }
        return students;
    }

    public boolean removeStudentFromClass(int studentId, int classId) {
        String sql = "DELETE FROM student_class WHERE student_id = ? AND class_id = ?";
        try (PreparedStatement ps = dbManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, classId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Remove student error: " + e.getMessage());
        }
        return false;
    }

    private ClassRoom mapClass(ResultSet rs) throws SQLException {
        ClassRoom cr = new ClassRoom();
        cr.setClassId(rs.getInt("class_id"));
        cr.setClassName(rs.getString("class_name"));
        cr.setTeacherId(rs.getInt("teacher_id"));
        cr.setTeacherName(rs.getString("teacher_name"));
        cr.setClassCode(rs.getString("class_code"));
        cr.setDescription(rs.getString("description"));
        cr.setCreatedAt(rs.getTimestamp("created_at"));
        return cr;
    }
}
