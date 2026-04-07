package com.quizapp;

import com.quizapp.database.DatabaseManager;
import com.quizapp.gui.common.LoginFrame;
import com.quizapp.server.QuizServer;

import javax.swing.*;

/**
 * Main entry point for the QuizArena application.
 *
 * USAGE:
 *   Run with arg "server"   → starts the socket quiz server only
 *   Run with no args        → starts the GUI client application
 *
 * For full functionality:
 *   1. Start the server:  java -cp ... com.quizapp.Main server
 *   2. Start the GUI:     java -cp ... com.quizapp.Main
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && "server".equalsIgnoreCase(args[0])) {
            // Run as socket server
            System.out.println("=== QuizArena Socket Server ===");
            DatabaseManager.getInstance(); // Init DB
            QuizServer server = new QuizServer();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
                DatabaseManager.getInstance().closeConnection();
            }));
            server.start();
        } else {
            // Run as GUI client
            SwingUtilities.invokeLater(() -> {
                System.out.println("=== QuizArena GUI Application ===");
                try {
                    DatabaseManager.getInstance(); // Init DB + create tables
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                        "Database connection failed!\n\n" +
                        "Please ensure MySQL is running and\n" +
                        "credentials in DatabaseConfig.java are correct.\n\n" +
                        "Error: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                new LoginFrame();
            });
        }
    }
}
