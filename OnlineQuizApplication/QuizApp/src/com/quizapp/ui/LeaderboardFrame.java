package com.quizapp.ui;

import com.quizapp.model.Quiz;
import com.quizapp.service.QuizService;
import com.quizapp.service.ScoreService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Displays a leaderboard of top scorers, either overall across all quizzes
 * or filtered to a specific quiz.
 */
public class LeaderboardFrame extends JFrame {

    private final ScoreService scoreService = new ScoreService();
    private final QuizService quizService = new QuizService();
    private final DefaultTableModel model;
    private final JComboBox<String> quizFilter;

    public LeaderboardFrame(Quiz preselected) {
        super("Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Leaderboard", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        List<Quiz> quizzes = quizService.getAllQuizzes();
        String[] options = new String[quizzes.size() + 1];
        options[0] = "Overall (All Quizzes)";
        for (int i = 0; i < quizzes.size(); i++) options[i + 1] = quizzes.get(i).getTitle();
        quizFilter = new JComboBox<>(options);
        if (preselected != null) {
            quizFilter.setSelectedItem(preselected.getTitle());
        }
        quizFilter.addActionListener(e -> refresh(quizzes));

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filterPanel.add(new JLabel("Show: "));
        filterPanel.add(quizFilter);
        top.add(filterPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel(new String[]{"Rank", "User", "Attempts", "Avg %", "Score"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(Theme.FONT_BOLD_BODY);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh(quizzes);
    }

    private void refresh(List<Quiz> quizzes) {
        model.setRowCount(0);
        int selectedIndex = quizFilter.getSelectedIndex();
        var entries = selectedIndex <= 0
                ? scoreService.getOverallLeaderboard(20)
                : scoreService.getQuizLeaderboard(quizzes.get(selectedIndex - 1).getId(), 20);

        int rank = 1;
        for (var entry : entries) {
            model.addRow(new Object[]{
                    rank++, entry.username(), entry.attemptCount(),
                    String.format("%.1f%%", entry.avgPercentage()), entry.totalScore()
            });
        }
        if (entries.isEmpty()) {
            model.addRow(new Object[]{"-", "No attempts recorded yet", "-", "-", "-"});
        }
    }
}
