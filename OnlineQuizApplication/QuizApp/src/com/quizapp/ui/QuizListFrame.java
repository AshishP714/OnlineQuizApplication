package com.quizapp.ui;

import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.service.QuizService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Lets a user browse available quizzes and start one. */
public class QuizListFrame extends JFrame {

    private final User user;
    private final QuizService quizService = new QuizService();
    private final DefaultListModel<Quiz> listModel = new DefaultListModel<>();
    private final JList<Quiz> quizList = new JList<>(listModel);

    public QuizListFrame(User user) {
        super("Available Quizzes");
        this.user = user;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 450);
        setLocationRelativeTo(null);
        buildUI();
        loadQuizzes();
    }

    private void buildUI() {
        JLabel title = new JLabel("Select a quiz to take", SwingConstants.CENTER);
        title.setFont(Theme.FONT_HEADING);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        quizList.setFont(Theme.FONT_BODY);
        quizList.setCellRenderer(new QuizCellRenderer());
        quizList.setFixedCellHeight(60);

        JButton startBtn = new JButton("Start Quiz");
        startBtn.setBackground(Theme.PRIMARY);
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> startSelectedQuiz());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadQuizzes());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(startBtn);
        bottom.add(refreshBtn);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(quizList), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadQuizzes() {
        listModel.clear();
        List<Quiz> quizzes = quizService.getAllQuizzes();
        for (Quiz q : quizzes) {
            q.setQuestions(quizService.getQuestionsForQuiz(q.getId()));
            if (!q.getQuestions().isEmpty()) {
                listModel.addElement(q);
            }
        }
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No quizzes with questions are available yet.",
                    "No Quizzes", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void startSelectedQuiz() {
        Quiz selected = quizList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a quiz first.");
            return;
        }
        new QuizTakingFrame(user, selected).setVisible(true);
        dispose();
    }

    private static class QuizCellRenderer extends JPanel implements ListCellRenderer<Quiz> {
        private final JLabel titleLabel = new JLabel();
        private final JLabel descLabel = new JLabel();

        QuizCellRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            titleLabel.setFont(Theme.FONT_BOLD_BODY);
            descLabel.setFont(Theme.FONT_SMALL);
            descLabel.setForeground(Theme.MUTED);
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.add(titleLabel);
            textPanel.add(descLabel);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Quiz> list, Quiz quiz, int index,
                                                        boolean isSelected, boolean cellHasFocus) {
            titleLabel.setText(quiz.getTitle() + "  [" + quiz.getDifficulty() + "]");
            int qCount = quiz.getQuestions() == null ? 0 : quiz.getQuestions().size();
            String timeInfo = quiz.getTimeLimitSeconds() > 0 ? (quiz.getTimeLimitSeconds() / 60) + " min limit" : "No time limit";
            descLabel.setText(qCount + " questions - " + timeInfo +
                    (quiz.getDescription() != null && !quiz.getDescription().isBlank() ? " - " + quiz.getDescription() : ""));
            setBackground(isSelected ? new Color(0xDD, 0xE6, 0xFF) : Color.WHITE);
            textPanel().setBackground(getBackground());
            return this;
        }

        private JPanel textPanel() {
            return (JPanel) getComponent(0);
        }
    }
}
