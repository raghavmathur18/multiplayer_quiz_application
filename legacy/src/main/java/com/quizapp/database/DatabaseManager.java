package com.quizapp.database;

import java.sql.*;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName(DatabaseConfig.DRIVER_CLASS);
            connection = DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            System.out.println("✅ Database connected successfully!");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null || instance.isConnectionClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public Connection getConnection() {
        if (isConnectionClosed()) {
            instance = new DatabaseManager();
        }
        return connection;
    }

    private void initializeDatabase() throws SQLException {
        String[] tables = {
            // Users table
            """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(150) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role ENUM('teacher','student') NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            // Class table
            """
            CREATE TABLE IF NOT EXISTS classes (
                class_id INT AUTO_INCREMENT PRIMARY KEY,
                class_name VARCHAR(200) NOT NULL,
                teacher_id INT NOT NULL,
                class_code VARCHAR(10) UNIQUE NOT NULL,
                description TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
            """,
            // Student_Class table
            """
            CREATE TABLE IF NOT EXISTS student_class (
                id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                class_id INT NOT NULL,
                joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_enrollment (student_id, class_id),
                FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
            )
            """,
            // Quiz table
            """
            CREATE TABLE IF NOT EXISTS quizzes (
                quiz_id INT AUTO_INCREMENT PRIMARY KEY,
                class_id INT NOT NULL,
                quiz_title VARCHAR(200) NOT NULL,
                time_limit_minutes INT DEFAULT 30,
                is_active BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
            )
            """,
            // Question table
            """
            CREATE TABLE IF NOT EXISTS questions (
                question_id INT AUTO_INCREMENT PRIMARY KEY,
                quiz_id INT NOT NULL,
                question_text TEXT NOT NULL,
                option1 VARCHAR(500) NOT NULL,
                option2 VARCHAR(500) NOT NULL,
                option3 VARCHAR(500) NOT NULL,
                option4 VARCHAR(500) NOT NULL,
                correct_answer INT NOT NULL COMMENT '1=option1, 2=option2, 3=option3, 4=option4',
                FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
            )
            """,
            // Result table
            """
            CREATE TABLE IF NOT EXISTS results (
                result_id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                quiz_id INT NOT NULL,
                score INT NOT NULL,
                total_questions INT NOT NULL,
                time_taken_seconds INT,
                attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
            )
            """,
            // Answer details table
            """
            CREATE TABLE IF NOT EXISTS answer_details (
                id INT AUTO_INCREMENT PRIMARY KEY,
                result_id INT NOT NULL,
                question_id INT NOT NULL,
                selected_answer INT,
                is_correct BOOLEAN,
                FOREIGN KEY (result_id) REFERENCES results(result_id) ON DELETE CASCADE,
                FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
            )
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
        }
        System.out.println("✅ Database tables initialized!");
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
