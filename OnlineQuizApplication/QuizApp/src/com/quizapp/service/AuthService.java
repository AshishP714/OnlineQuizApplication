package com.quizapp.service;

import com.quizapp.db.DatabaseManager;
import com.quizapp.model.User;
import com.quizapp.util.PasswordUtil;

import java.sql.*;

public class AuthService {

	public User register(String username, String password, boolean asAdmin) throws AuthException {
		if (username == null || username.isBlank())
			throw new AuthException("Username cannot be empty.");
		if (password == null || password.length() < 4)
			throw new AuthException("Password must be at least 4 characters.");

		Connection conn = DatabaseManager.getConnection();
		try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
			check.setString(1, username);
			try (ResultSet rs = check.executeQuery()) {
				if (rs.next())
					throw new AuthException("Username already exists.");
			}
		} catch (SQLException e) {
			throw new AuthException("Database error: " + e.getMessage());
		}

		String salt = PasswordUtil.generateSalt();
		String hash = PasswordUtil.hash(password, salt);

		String sql = "INSERT INTO users(username, password_hash, salt, is_admin) VALUES (?,?,?,?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, username);
			ps.setString(2, hash);
			ps.setString(3, salt);
			ps.setInt(4, asAdmin ? 1 : 0);
			ps.executeUpdate();
			int id = DatabaseManager.lastInsertId();
			return new User(id, username, hash, salt, asAdmin);
		} catch (SQLException e) {
			throw new AuthException("Failed to create account: " + e.getMessage());
		}
	}

	public static class AuthException extends Exception {
		public AuthException(String message) {
			super(message);
		}
	}
	
	public User login(String username, String password) throws AuthException {
		if (username == null || username.isBlank())
			throw new AuthException("Username cannot be empty.");
		if (password == null || password.length() < 4)
			throw new AuthException("Password must be at least 4 characters.");

		String sql = "SELECT id, username, password_hash, salt, is_admin FROM users WHERE username = ?";
		try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
			ps.setString(1, username);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next())
					throw new AuthException("Invalid username or password.");
				String hash = rs.getString("password_hash");
				String salt = rs.getString("salt");
				if (!PasswordUtil.verify(password, hash, salt))
					throw new AuthException("Invalid username or password.");
				return new User(rs.getInt("id"), rs.getString("username"), hash, salt, rs.getInt("is_admin") == 1);
			}
		} catch (SQLException e) {
			throw new AuthException("Database error: " + e.getMessage());
		}
	}
}