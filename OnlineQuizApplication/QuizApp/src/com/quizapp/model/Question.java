package com.quizapp.model;

import java.util.List;

public class Question {
    private int id;
    private int quizId;
    private String text;
    private List<String> options;
    private List<Integer> correctIndices;
    private boolean multipleAnswer;

    public Question(int id, int quizId, String text, List<String> options,
                     List<Integer> correctIndices, boolean multipleAnswer) {
        this.id = id;
        this.quizId = quizId;
        this.text = text;
        this.options = options;
        this.correctIndices = correctIndices;
        this.multipleAnswer = multipleAnswer;
    }

    public int getId() { return id; }
    public int getQuizId() { return quizId; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public List<Integer> getCorrectIndices() { return correctIndices; }
    public boolean isMultipleAnswer() { return multipleAnswer; }

    public void setId(int id) { this.id = id; }

    public boolean isCorrect(List<Integer> selectedIndices) {
        if (selectedIndices == null) return false;
        return new java.util.HashSet<>(selectedIndices).equals(new java.util.HashSet<>(correctIndices));
    }

    @Override
    public String toString() {
        return text;
    }
}