package com.quizapp.service;

import com.quizapp.db.DatabaseManager;
import com.quizapp.model.QuizAttempt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ScoreService {

    public void recordAttempt(int userId, int quizId, int score, int totalQuestions) {
        String sql = "INSERT INTO attempts(user_id, quiz_id, score, total_questions, attempt_date) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            ps.setInt(3, score);
            ps.setInt(4, totalQuestions);
            ps.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record attempt", e);
        }
    }

    public List<QuizAttempt> getHistoryForUser(int userId) {
        List<QuizAttempt> attempts = new ArrayList<>();
        String sql = """
            SELECT a.*, q.title AS quiz_title FROM attempts a
            JOIN quizzes q ON a.quiz_id = q.id
            WHERE a.user_id = ?
            ORDER BY a.attempt_date DESC
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load history", e);
        }
        return attempts;
    }

    public record LeaderboardEntry(String username, int attemptCount, double avgPercentage, int totalScore) { }

    public List<LeaderboardEntry> getOverallLeaderboard(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = """
            SELECT u.username,
                   COUNT(a.id) AS attempt_count,
                   AVG( (a.score * 100.0) / NULLIF(a.total_questions,0) ) AS avg_pct,
                   SUM(a.score) AS total_score
            FROM attempts a
            JOIN users u ON a.user_id = u.id
            GROUP BY u.id
            ORDER BY avg_pct DESC, total_score DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new LeaderboardEntry(
                            rs.getString("username"),
                            rs.getInt("attempt_count"),
                            rs.getDouble("avg_pct"),
                            rs.getInt("total_score")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load leaderboard", e);
        }
        return entries;
    }

    public List<LeaderboardEntry> getQuizLeaderboard(int quizId, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String sql = """
            SELECT u.username,
                   COUNT(a.id) AS attempt_count,
                   MAX( (a.score * 100.0) / NULLIF(a.total_questions,0) ) AS avg_pct,
                   MAX(a.score) AS total_score
            FROM attempts a
            JOIN users u ON a.user_id = u.id
            WHERE a.quiz_id = ?
            GROUP BY u.id
            ORDER BY avg_pct DESC, total_score DESC
            LIMIT ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new LeaderboardEntry(
                            rs.getString("username"),
                            rs.getInt("attempt_count"),
                            rs.getDouble("avg_pct"),
                            rs.getInt("total_score")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load quiz leaderboard", e);
        }
        return entries;
    }

    private QuizAttempt mapAttempt(ResultSet rs) throws SQLException {
        return new QuizAttempt(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("quiz_id"),
                rs.getString("quiz_title"), rs.getInt("score"), rs.getInt("total_questions"),
                rs.getString("attempt_date"));
    }
}