package com.quizapp.ui;

import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.service.QuizService;

import javax.swing.*;
import java.awt.*;

/** Dialog for creating a new quiz or editing an existing quiz's metadata. */
public class QuizEditorDialog extends JDialog {

    private final JTextField titleField = new JTextField(22);
    private final JTextArea descArea = new JTextArea(3, 22);
    private final JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
    private final JSpinner timeLimitSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 30));

    private final QuizService quizService = new QuizService();
    private final User creator;
    private final Quiz existingQuiz;
    private boolean saved = false;
    private Quiz resultQuiz;

    public QuizEditorDialog(Frame owner, User creator, Quiz existingQuiz) {
        super(owner, existingQuiz == null ? "Create Quiz" : "Edit Quiz", true);
        this.creator = creator;
        this.existingQuiz = existingQuiz;
        setSize(420, 340);
        setLocationRelativeTo(owner);
        buildUI();
        if (existingQuiz != null) populate();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descArea.setLineWrap(true);
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Difficulty:"), gbc);
        gbc.gridx = 1;
        panel.add(difficultyBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Time limit (sec, 0=none):"), gbc);
        gbc.gridx = 1;
        panel.add(timeLimitSpinner, gbc);

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(Theme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> save());

        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(saveBtn, gbc);

        add(panel);
    }

    private void populate() {
        titleField.setText(existingQuiz.getTitle());
        descArea.setText(existingQuiz.getDescription());
        difficultyBox.setSelectedItem(existingQuiz.getDifficulty());
        timeLimitSpinner.setValue(existingQuiz.getTimeLimitSeconds());
    }

    private void save() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String desc = descArea.getText().trim();
        String difficulty = (String) difficultyBox.getSelectedItem();
        int timeLimit = (Integer) timeLimitSpinner.getValue();

        if (existingQuiz == null) {
            resultQuiz = quizService.createQuiz(title, desc, difficulty, timeLimit, creator.getId());
        } else {
            quizService.updateQuiz(existingQuiz.getId(), title, desc, difficulty, timeLimit);
            resultQuiz = quizService.getQuizById(existingQuiz.getId());
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Quiz getResultQuiz() { return resultQuiz; }
}
