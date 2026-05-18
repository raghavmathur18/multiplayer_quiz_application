package com.quizapp.models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClassRoom implements Serializable {
    private static final long serialVersionUID = 1L;

    private int classId;
    private String className;
    private int teacherId;
    private String teacherName;
    private String classCode;
    private String description;
    private Timestamp createdAt;
    private List<User> students = new ArrayList<>();
    private List<Quiz> quizzes = new ArrayList<>();

    public ClassRoom() {}

    public ClassRoom(int classId, String className, int teacherId, String classCode) {
        this.classId = classId;
        this.className = className;
        this.teacherId = teacherId;
        this.classCode = classCode;
    }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public List<User> getStudents() { return students; }
    public void setStudents(List<User> students) { this.students = students; }
    public List<Quiz> getQuizzes() { return quizzes; }
    public void setQuizzes(List<Quiz> quizzes) { this.quizzes = quizzes; }

    @Override
    public String toString() { return className + " [" + classCode + "]"; }
}
