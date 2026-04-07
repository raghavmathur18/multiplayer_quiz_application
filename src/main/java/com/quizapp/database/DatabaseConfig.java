package com.quizapp.database;

public class DatabaseConfig {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/quizapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";

    public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    
    // Server config
    public static final int SERVER_PORT = 9090;
    public static final String SERVER_HOST = "localhost";
}
