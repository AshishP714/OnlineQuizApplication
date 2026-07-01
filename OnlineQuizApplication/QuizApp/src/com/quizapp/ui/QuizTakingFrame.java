package com.quizapp.ui;

import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.service.ScoreService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Presents quiz questions one at a time with immediate correct/incorrect feedback,
 * tracks the running score, and supports an optional overall time limit.
 */
public class QuizTakingFrame extends JFrame {

    private final User user;
    private final Quiz quiz;
    private final List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;

    private final JLabel progressLabel = new JLabel();
    private final JLabel timerLabel = new JLabel();
    private final JLabel questionLabel = new JLabel();
    private final JPanel optionsPanel = new JPanel();
    private final JLabel feedbackLabel = new JLabel(" ");
    private final JButton submitBtn = new JButton("Submit Answer");
    private final JButton nextBtn = new JButton("Next Question");
    private final JProgressBar progressBar = new JProgressBar();

    private List<AbstractButton> optionButtons = new ArrayList<>();
    private boolean answered = false;

    private Timer swingTimer;
    private int secondsRemaining;

    public QuizTakingFrame(User user, Quiz quiz) {
        super("Taking Quiz: " + quiz.getTitle());
        this.user = user;
        this.quiz = quiz;
        this.questions = new ArrayList<>(quiz.getQuestions());
        Collections.shuffle(this.questions); // random question order each attempt

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        buildUI();
        showQuestion();
        startTimerIfNeeded();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout());
        progressLabel.setFont(Theme.FONT_SMALL);
        timerLabel.setFont(Theme.FONT_BOLD_BODY);
        timerLabel.setForeground(Theme.ERROR);
        top.add(progressLabel, BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);
        progressBar.setMaximum(questions.size());

        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.add(top, BorderLayout.NORTH);
        topWrap.add(progressBar, BorderLayout.SOUTH);

        questionLabel.setFont(Theme.FONT_HEADING);
        questionLabel.setVerticalAlignment(SwingConstants.TOP);

        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        feedbackLabel.setFont(Theme.FONT_BOLD_BODY);

        submitBtn.setBackground(Theme.PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> submitAnswer());

        nextBtn.setEnabled(false);
        nextBtn.addActionListener(e -> nextQuestion());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitBtn);
        buttonPanel.add(nextBtn);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(questionLabel, BorderLayout.NORTH);
        center.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(feedbackLabel, BorderLayout.NORTH);
        south.add(buttonPanel, BorderLayout.SOUTH);

        add(topWrap, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void startTimerIfNeeded() {
        if (quiz.getTimeLimitSeconds() <= 0) {
            timerLabel.setText("");
            return;
        }
        secondsRemaining = quiz.getTimeLimitSeconds();
        updateTimerLabel();
        swingTimer = new Timer(true);
        swingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsRemaining--;
                SwingUtilities.invokeLater(() -> {
                    updateTimerLabel();
                    if (secondsRemaining <= 0) {
                        swingTimer.cancel();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(QuizTakingFrame.this,
                                    "Time's up! Submitting your quiz with current progress.");
                            finishQuiz();
                        });
                    }
                });
            }
        }, 1000, 1000);
    }

    private void updateTimerLabel() {
        int m = secondsRemaining / 60, s = secondsRemaining % 60;
        timerLabel.setText(String.format("Time left: %02d:%02d", m, s));
    }

    private void showQuestion() {
        answered = false;
        submitBtn.setEnabled(true);
        nextBtn.setEnabled(false);
        feedbackLabel.setText(" ");
        feedbackLabel.setForeground(Theme.TEXT);

        Question q = questions.get(currentIndex);
        progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size() + "   |   Score: " + score);
        progressBar.setValue(currentIndex);
        questionLabel.setText("<html><body style='width: 480px'>" + escape(q.getText()) + "</body></html>");

        optionsPanel.removeAll();
        optionButtons = new ArrayList<>();
        ButtonGroup group = q.isMultipleAnswer() ? null : new ButtonGroup();
        for (int i = 0; i < q.getOptions().size(); i++) {
            AbstractButton btn = q.isMultipleAnswer()
                    ? new JCheckBox(q.getOptions().get(i))
                    : new JRadioButton(q.getOptions().get(i));
            btn.setFont(Theme.FONT_BODY);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (group != null) group.add(btn);
            optionButtons.add(btn);
            optionsPanel.add(btn);
            optionsPanel.add(Box.createVerticalStrut(4));
        }
        if (q.isMultipleAnswer()) {
            JLabel hint = new JLabel("(Select all correct answers)");
            hint.setFont(Theme.FONT_SMALL);
            hint.setForeground(Theme.MUTED);
            optionsPanel.add(hint);
        }
        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void submitAnswer() {
        if (answered) return;
        Question q = questions.get(currentIndex);
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < optionButtons.size(); i++) {
            if (optionButtons.get(i).isSelected()) selected.add(i);
        }
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an answer before submitting.");
            return;
        }

        boolean correct = q.isCorrect(selected);
        answered = true;
        submitBtn.setEnabled(false);
        nextBtn.setEnabled(true);

        for (AbstractButton b : optionButtons) b.setEnabled(false);

        if (correct) {
            score++;
            feedbackLabel.setText("Correct!");
            feedbackLabel.setForeground(Theme.SUCCESS);
        } else {
            StringBuilder correctText = new StringBuilder();
            for (int idx : q.getCorrectIndices()) {
                if (correctText.length() > 0) correctText.append(", ");
                correctText.append(q.getOptions().get(idx));
            }
            feedbackLabel.setText("Incorrect. Correct answer: " + correctText);
            feedbackLabel.setForeground(Theme.ERROR);
        }
        progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size() + "   |   Score: " + score);

        if (currentIndex == questions.size() - 1) {
            nextBtn.setText("Finish Quiz");
        }
    }

    private void nextQuestion() {
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        if (swingTimer != null) swingTimer.cancel();
        new ScoreService().recordAttempt(user.getId(), quiz.getId(), score, questions.size());
        new ResultFrame(user, quiz, score, questions.size()).setVisible(true);
        dispose();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
