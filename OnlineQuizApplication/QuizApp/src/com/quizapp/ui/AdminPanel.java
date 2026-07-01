package com.quizapp.ui;

import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.User;
import com.quizapp.service.QuizService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Administrator interface for creating, editing, and deleting quizzes and their questions. */
public class AdminPanel extends JFrame {

    private final User admin;
    private final QuizService quizService = new QuizService();

    private final DefaultListModelWrapper<Quiz> quizListModel = new DefaultListModelWrapper<>();
    private final JList<Quiz> quizList = new JList<>(quizListModel.model);

    private final DefaultListModelWrapper<Question> questionListModel = new DefaultListModelWrapper<>();
    private final JList<Question> questionList = new JList<>(questionListModel.model);

    public AdminPanel(User admin) {
        super("Admin - Manage Quizzes");
        this.admin = admin;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);
        buildUI();
        loadQuizzes();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: quiz list + quiz-level buttons
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Quizzes"));
        quizList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) onQuizSelected(); });
        left.add(new JScrollPane(quizList), BorderLayout.CENTER);

        JPanel quizButtons = new JPanel(new GridLayout(3, 1, 3, 3));
        JButton addQuizBtn = new JButton("Add Quiz");
        JButton editQuizBtn = new JButton("Edit Quiz");
        JButton deleteQuizBtn = new JButton("Delete Quiz");
        addQuizBtn.addActionListener(e -> addQuiz());
        editQuizBtn.addActionListener(e -> editQuiz());
        deleteQuizBtn.addActionListener(e -> deleteQuiz());
        quizButtons.add(addQuizBtn);
        quizButtons.add(editQuizBtn);
        quizButtons.add(deleteQuizBtn);
        left.add(quizButtons, BorderLayout.SOUTH);

        // Right: question list for the selected quiz + question-level buttons
        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(BorderFactory.createTitledBorder("Questions in Selected Quiz"));
        right.add(new JScrollPane(questionList), BorderLayout.CENTER);

        JPanel qButtons = new JPanel(new GridLayout(3, 1, 3, 3));
        JButton addQBtn = new JButton("Add Question");
        JButton editQBtn = new JButton("Edit Question");
        JButton deleteQBtn = new JButton("Delete Question");
        addQBtn.addActionListener(e -> addQuestion());
        editQBtn.addActionListener(e -> editQuestion());
        deleteQBtn.addActionListener(e -> deleteQuestion());
        qButtons.add(addQBtn);
        qButtons.add(editQBtn);
        qButtons.add(deleteQBtn);
        right.add(qButtons, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadQuizzes() {
        quizListModel.model.clear();
        for (Quiz q : quizService.getAllQuizzes()) quizListModel.model.addElement(q);
        questionListModel.model.clear();
    }

    private void onQuizSelected() {
        questionListModel.model.clear();
        Quiz selected = quizList.getSelectedValue();
        if (selected != null) {
            for (Question q : quizService.getQuestionsForQuiz(selected.getId())) {
                questionListModel.model.addElement(q);
            }
        }
    }

    private void addQuiz() {
        QuizEditorDialog dialog = new QuizEditorDialog(this, admin, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadQuizzes();
    }

    private void editQuiz() {
        Quiz selected = quizList.getSelectedValue();
        if (selected == null) { warnNoSelection("quiz"); return; }
        QuizEditorDialog dialog = new QuizEditorDialog(this, admin, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadQuizzes();
    }

    private void deleteQuiz() {
        Quiz selected = quizList.getSelectedValue();
        if (selected == null) { warnNoSelection("quiz"); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete quiz \"" + selected.getTitle() + "\" and all its questions? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            quizService.deleteQuiz(selected.getId());
            loadQuizzes();
        }
    }

    private void addQuestion() {
        Quiz selected = quizList.getSelectedValue();
        if (selected == null) { warnNoSelection("quiz first, then add a question to it"); return; }
        QuestionEditorDialog dialog = new QuestionEditorDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            quizService.addQuestion(selected.getId(), dialog.getResultText(), dialog.getResultOptions(),
                    dialog.getResultCorrect(), dialog.isResultMultiple());
            onQuizSelected();
        }
    }

    private void editQuestion() {
        Question selected = questionList.getSelectedValue();
        if (selected == null) { warnNoSelection("question"); return; }
        QuestionEditorDialog dialog = new QuestionEditorDialog(this, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Question updated = new Question(selected.getId(), selected.getQuizId(),
                    dialog.getResultText(), dialog.getResultOptions(), dialog.getResultCorrect(), dialog.isResultMultiple());
            quizService.updateQuestion(updated);
            onQuizSelected();
        }
    }

    private void deleteQuestion() {
        Question selected = questionList.getSelectedValue();
        if (selected == null) { warnNoSelection("question"); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this question?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            quizService.deleteQuestion(selected.getId());
            onQuizSelected();
        }
    }

    private void warnNoSelection(String what) {
        JOptionPane.showMessageDialog(this, "Please select a " + what + " first.");
    }

    /** Small helper to avoid raw DefaultListModel boilerplate at call sites. */
    private static class DefaultListModelWrapper<T> {
        final DefaultListModel<T> model = new DefaultListModel<>();
    }
}
