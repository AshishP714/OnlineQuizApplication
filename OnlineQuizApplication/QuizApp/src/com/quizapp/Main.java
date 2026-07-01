package com.quizapp;

import com.quizapp.db.DatabaseManager;
import com.quizapp.ui.LoginFrame;

import javax.swing.*;

/**
 * Entry point for the Online Quiz Application.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize DB eagerly so any startup errors surface immediately.
        DatabaseManager.getConnection();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));

        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close));
    }
}
