package com.quizapp.db;

import com.quizapp.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the MySQL database connection and schema initialization
 * for the Online Quiz Application.
 * 
 * Configure MySQL connection by setting environment variables:
 * - DB_HOST (default: localhost)
 * - DB_PORT (default: 3306)
 * - DB_NAME (default: quizapp_db)
 * - DB_USER (default: root)
 * - DB_PASSWORD (default: empty)
 */
public final class DatabaseManager {

    private static final String DB_HOST = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
    private static final String DB_PORT = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "quizapp_db";
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";
    
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME 
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    
    private static Connection connection;

    private DatabaseManager() { }

    /** Returns a shared JDBC connection, creating and initializing the DB if needed. */
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                initSchema();
                seedAdmin();
            } catch (ClassNotFoundException | SQLException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
        return connection;
    }

    private static void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(255) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    is_admin INTEGER NOT NULL DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS quizzes (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    description TEXT,
                    difficulty VARCHAR(50) DEFAULT 'Medium',
                    time_limit_seconds INTEGER DEFAULT 0,
                    created_by INTEGER,
                    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    quiz_id INTEGER NOT NULL,
                    text TEXT NOT NULL,
                    options LONGTEXT NOT NULL,
                    correct_indices TEXT NOT NULL,
                    multiple_answer INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS attempts (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    user_id INTEGER NOT NULL,
                    quiz_id INTEGER NOT NULL,
                    score INTEGER NOT NULL,
                    total_questions INTEGER NOT NULL,
                    attempt_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);
        }
    }

    /** Creates a default admin account (admin/admin123) on first run, for convenience. */
    private static void seedAdmin() throws SQLException {
        try (Statement st = connection.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) AS c FROM users WHERE is_admin = 1")) {
            if (rs.next() && rs.getInt("c") == 0) {
                String salt = PasswordUtil.generateSalt();
                String hash = PasswordUtil.hash("admin123", salt);
                try (var ps = connection.prepareStatement(
                        "INSERT INTO users(username, password_hash, salt, is_admin) VALUES (?,?,?,1)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, hash);
                    ps.setString(3, salt);
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Returns the row id generated by the most recent INSERT on this connection. */
    public static synchronized int lastInsertId() {
        try (Statement st = getConnection().createStatement();
             var rs = st.executeQuery("SELECT LAST_INSERT_ID() AS id")) {
            rs.next();
            return rs.getInt("id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch last insert id", e);
        }
    }

    public static synchronized void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) { }
            connection = null;
        }
    }
}