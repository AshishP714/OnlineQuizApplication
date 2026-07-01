package com.quizapp.ui;

import com.quizapp.model.Quiz;
import com.quizapp.model.User;

import javax.swing.*;
import java.awt.*;

/** Displays the final score summary after a quiz attempt. */
public class ResultFrame extends JFrame {

    public ResultFrame(User user, Quiz quiz, int score, int total) {
        super("Quiz Results");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 350);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);

        double pct = total == 0 ? 0 : (score * 100.0) / total;

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Quiz Complete!");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.PRIMARY_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quizName = new JLabel(quiz.getTitle());
        quizName.setFont(Theme.FONT_BODY);
        quizName.setForeground(Theme.MUTED);
        quizName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel(score + " / " + total + "  (" + String.format("%.1f", pct) + "%)");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        scoreLabel.setForeground(pct >= 60 ? Theme.SUCCESS : Theme.ERROR);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel remark = new JLabel(remarkFor(pct), SwingConstants.CENTER);
        remark.setFont(Theme.FONT_BODY);
        remark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backBtn = new JButton("Back to Main Menu");
        backBtn.setBackground(Theme.PRIMARY);
        backBtn.setForeground(Color.WHITE);
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> {
            dispose();
            new MainMenuFrame(user).setVisible(true);
        });

        JButton historyBtn = new JButton("View History");
        historyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyBtn.addActionListener(e -> new HistoryFrame(user).setVisible(true));

        root.add(title);
        root.add(quizName);
        root.add(scoreLabel);
        root.add(remark);
        root.add(Box.createVerticalStrut(20));
        root.add(backBtn);
        root.add(Box.createVerticalStrut(8));
        root.add(historyBtn);

        add(root, BorderLayout.CENTER);
    }

    private String remarkFor(double pct) {
        if (pct == 100) return "Perfect score! Outstanding work.";
        if (pct >= 80) return "Great job!";
        if (pct >= 60) return "Good effort, keep practicing.";
        if (pct >= 40) return "Not bad - try again to improve.";
        return "Keep studying and try again!";
    }
}
