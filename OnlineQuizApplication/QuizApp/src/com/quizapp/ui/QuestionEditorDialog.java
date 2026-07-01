package com.quizapp.ui;

import com.quizapp.model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for adding or editing a single question: its text, a variable
 * number of options, which option(s) are correct, and single/multiple mode.
 */
public class QuestionEditorDialog extends JDialog {

    private final JTextArea questionText = new JTextArea(3, 30);
    private final JCheckBox multipleAnswerBox = new JCheckBox("Allow multiple correct answers");
    private final JPanel optionsContainer = new JPanel();
    private final List<OptionRow> optionRows = new ArrayList<>();

    private boolean saved = false;
    private String resultText;
    private List<String> resultOptions;
    private List<Integer> resultCorrect;
    private boolean resultMultiple;

    public QuestionEditorDialog(Frame owner, Question existing) {
        super(owner, existing == null ? "Add Question" : "Edit Question", true);
        setSize(520, 480);
        setLocationRelativeTo(owner);
        buildUI();
        if (existing != null) populate(existing);
        else { addOptionRow(); addOptionRow(); addOptionRow(); addOptionRow(); }
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(new JLabel("Question text:"), BorderLayout.NORTH);
        questionText.setLineWrap(true);
        questionText.setWrapStyleWord(true);
        top.add(new JScrollPane(questionText), BorderLayout.CENTER);
        top.add(multipleAnswerBox, BorderLayout.SOUTH);

        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        JScrollPane optionsScroll = new JScrollPane(optionsContainer);
        optionsScroll.setBorder(BorderFactory.createTitledBorder("Options (check the correct answer(s))"));

        JButton addOptionBtn = new JButton("+ Add Option");
        addOptionBtn.addActionListener(e -> addOptionRow());

        JButton saveBtn = new JButton("Save Question");
        saveBtn.setBackground(Theme.PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> save());

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(addOptionBtn, BorderLayout.WEST);
        bottom.add(saveBtn, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(optionsScroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void addOptionRow() {
        OptionRow row = new OptionRow();
        optionRows.add(row);
        optionsContainer.add(row.panel);
        optionsContainer.revalidate();
        optionsContainer.repaint();
    }

    private void populate(Question q) {
        questionText.setText(q.getText());
        multipleAnswerBox.setSelected(q.isMultipleAnswer());
        for (int i = 0; i < q.getOptions().size(); i++) {
            addOptionRow();
            OptionRow row = optionRows.get(i);
            row.field.setText(q.getOptions().get(i));
            row.checkBox.setSelected(q.getCorrectIndices().contains(i));
        }
    }

    private void save() {
        String text = questionText.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Question text is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<String> options = new ArrayList<>();
        List<Integer> correct = new ArrayList<>();
        for (int i = 0; i < optionRows.size(); i++) {
            OptionRow row = optionRows.get(i);
            String val = row.field.getText().trim();
            if (val.isEmpty()) continue;
            options.add(val);
            if (row.checkBox.isSelected()) correct.add(options.size() - 1);
        }
        if (options.size() < 2) {
            JOptionPane.showMessageDialog(this, "Please provide at least 2 options.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (correct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please mark at least one correct answer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean multiple = multipleAnswerBox.isSelected();
        if (!multiple && correct.size() > 1) {
            JOptionPane.showMessageDialog(this, "Multiple correct answers selected - enabling multi-answer mode.",
                    "Notice", JOptionPane.INFORMATION_MESSAGE);
            multiple = true;
        }

        resultText = text;
        resultOptions = options;
        resultCorrect = correct;
        resultMultiple = multiple;
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public String getResultText() { return resultText; }
    public List<String> getResultOptions() { return resultOptions; }
    public List<Integer> getResultCorrect() { return resultCorrect; }
    public boolean isResultMultiple() { return resultMultiple; }

    private static class OptionRow {
        final JPanel panel = new JPanel(new BorderLayout(5, 0));
        final JTextField field = new JTextField();
        final JCheckBox checkBox = new JCheckBox("Correct");

        OptionRow() {
            panel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            panel.add(field, BorderLayout.CENTER);
            panel.add(checkBox, BorderLayout.EAST);
        }
    }
}
