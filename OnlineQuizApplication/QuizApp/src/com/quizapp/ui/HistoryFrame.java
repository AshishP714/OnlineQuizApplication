package com.quizapp.ui;

import com.quizapp.model.QuizAttempt;
import com.quizapp.model.User;
import com.quizapp.service.ScoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/** Shows a table of the current user's past quiz attempts and scores. */
public class HistoryFrame extends JFrame {

    public HistoryFrame(User user) {
        super("My Quiz History - " + user.getUsername());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 420);
        setLocationRelativeTo(null);

        String[] columns = {"Quiz", "Score", "Percentage", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<QuizAttempt> history = new ScoreService().getHistoryForUser(user.getId());
        for (QuizAttempt a : history) {
            model.addRow(new Object[]{
                    a.getQuizTitle(),
                    a.getScore() + " / " + a.getTotalQuestions(),
                    String.format("%.1f%%", a.getPercentage()),
                    a.getAttemptDate()
            });
        }

        JTable table = new JTable(model);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(Theme.FONT_BOLD_BODY);

        JLabel title = new JLabel("Quiz Attempt History", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (history.isEmpty()) {
            JLabel empty = new JLabel("You haven't taken any quizzes yet.", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_BODY);
            add(title, "North");
            add(empty, "Center");
        } else {
            add(title, "North");
            add(new JScrollPane(table), "Center");
        }
    }
}
