package com.quizapp.model;

import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private String description;
    private String difficulty;
    private int timeLimitSeconds;
    private int createdBy;
    private List<Question> questions;

    public Quiz(int id, String title, String description, String difficulty,
                int timeLimitSeconds, int createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.timeLimitSeconds = timeLimitSeconds;
        this.createdBy = createdBy;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public int getCreatedBy() { return createdBy; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    @Override
    public String toString() {
        return title + " [" + difficulty + "]";
    }
}