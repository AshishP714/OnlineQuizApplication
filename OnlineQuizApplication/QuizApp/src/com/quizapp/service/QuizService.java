package com.quizapp.service;

import com.quizapp.db.DatabaseManager;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles CRUD operations for quizzes and their questions.
 * Option lists are stored as a "|"-delimited string; correct answer indices
 * as a ","-delimited string of 0-based option indices.
 */
public class QuizService {

    private static final String OPT_DELIM = "\\|\\|";
    private static final String OPT_JOIN = "||";

    // ---------- Quiz CRUD ----------

    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quizzes ORDER BY id DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                quizzes.add(mapQuiz(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load quizzes", e);
        }
        return quizzes;
    }

    public Quiz getQuizById(int id) {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapQuiz(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load quiz", e);
        }
        return null;
    }

    public Quiz createQuiz(String title, String description, String difficulty,
                            int timeLimitSeconds, int createdBy) {
        String sql = "INSERT INTO quizzes(title, description, difficulty, time_limit_seconds, created_by) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, difficulty);
            ps.setInt(4, timeLimitSeconds);
            ps.setInt(5, createdBy);
            ps.executeUpdate();
            int id = DatabaseManager.lastInsertId();
            return new Quiz(id, title, description, difficulty, timeLimitSeconds, createdBy);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create quiz", e);
        }
    }

    public void updateQuiz(int id, String title, String description, String difficulty, int timeLimitSeconds) {
        String sql = "UPDATE quizzes SET title=?, description=?, difficulty=?, time_limit_seconds=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, difficulty);
            ps.setInt(4, timeLimitSeconds);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update quiz", e);
        }
    }

    public void deleteQuiz(int id) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete quiz", e);
        }
    }

    // ---------- Question CRUD ----------

    public List<Question> getQuestionsForQuiz(int quizId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapQuestion(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load questions", e);
        }
        return questions;
    }

    public Question addQuestion(int quizId, String text, List<String> options,
                                 List<Integer> correctIndices, boolean multipleAnswer) {
        String sql = "INSERT INTO questions(quiz_id, text, options, correct_indices, multiple_answer) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setString(2, text);
            ps.setString(3, String.join(OPT_JOIN, options));
            ps.setString(4, correctIndices.stream().map(String::valueOf).collect(Collectors.joining(",")));
            ps.setInt(5, multipleAnswer ? 1 : 0);
            ps.executeUpdate();
            int id = DatabaseManager.lastInsertId();
            return new Question(id, quizId, text, options, correctIndices, multipleAnswer);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add question", e);
        }
    }

    public void updateQuestion(Question q) {
        String sql = "UPDATE questions SET text=?, options=?, correct_indices=?, multiple_answer=? WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, q.getText());
            ps.setString(2, String.join(OPT_JOIN, q.getOptions()));
            ps.setString(3, q.getCorrectIndices().stream().map(String::valueOf).collect(Collectors.joining(",")));
            ps.setInt(4, q.isMultipleAnswer() ? 1 : 0);
            ps.setInt(5, q.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update question", e);
        }
    }

    public void deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete question", e);
        }
    }

    // ---------- Mapping helpers ----------

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        return new Quiz(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getString("difficulty"), rs.getInt("time_limit_seconds"), rs.getInt("created_by"));
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        List<String> options = new ArrayList<>(Arrays.asList(rs.getString("options").split(OPT_DELIM)));
        List<Integer> correct = new ArrayList<>();
        for (String s : rs.getString("correct_indices").split(",")) {
            if (!s.isBlank()) correct.add(Integer.parseInt(s.trim()));
        }
        return new Question(rs.getInt("id"), rs.getInt("quiz_id"), rs.getString("text"),
                options, correct, rs.getInt("multiple_answer") == 1);
    }
}
