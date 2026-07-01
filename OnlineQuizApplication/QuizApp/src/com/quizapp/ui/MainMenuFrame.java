package com.quizapp.ui;

import com.quizapp.model.User;

import javax.swing.*;
import java.awt.*;

/** Main dashboard shown after login. Options differ for admins vs regular users. */
public class MainMenuFrame extends JFrame {

    private final User user;

    public MainMenuFrame(User user) {
        super("Online Quiz Application - " + user.getUsername());
        this.user = user;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel welcome = new JLabel("Welcome, " + user.getUsername() + "!");
        welcome.setFont(Theme.FONT_TITLE);
        welcome.setForeground(Theme.PRIMARY_DARK);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel role = new JLabel(user.isAdmin() ? "Administrator" : "Quiz Taker");
        role.setFont(Theme.FONT_SMALL);
        role.setForeground(Theme.MUTED);
        role.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(welcome);
        root.add(role);
        root.add(Box.createVerticalStrut(25));

        root.add(menuButton("Take a Quiz", () -> { new QuizListFrame(user).setVisible(true); }));
        root.add(Box.createVerticalStrut(10));
        root.add(menuButton("My Quiz History", () -> { new HistoryFrame(user).setVisible(true); }));
        root.add(Box.createVerticalStrut(10));
        root.add(menuButton("Leaderboard", () -> { new LeaderboardFrame(null).setVisible(true); }));

        if (user.isAdmin()) {
            root.add(Box.createVerticalStrut(10));
            root.add(menuButton("Manage Quizzes (Admin)", () -> { new AdminPanel(user).setVisible(true); }));
        }

        root.add(Box.createVerticalStrut(20));
        root.add(menuButton("Log Out", this::logout));

        add(root, BorderLayout.CENTER);
    }

    private JButton menuButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BOLD_BODY);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(300, 42));
        btn.setBackground(Theme.PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
