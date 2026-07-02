package com.quizapp.model;

public class QuizAttempt {
    private int id;
    private int userId;
    private int quizId;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private String attemptDate; // ISO timestamp string

    public QuizAttempt(int id, int userId, int quizId, String quizTitle,
                        int score, int totalQuestions, String attemptDate) {
        this.id = id;
        this.userId = userId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.attemptDate = attemptDate;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public String getAttemptDate() { return attemptDate; }

    public double getPercentage() {
        return totalQuestions == 0 ? 0 : (score * 100.0) / totalQuestions;
    }
}
