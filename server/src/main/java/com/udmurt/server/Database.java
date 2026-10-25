package com.udmurt.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище базы данных на SQLite.
 * Пользователи аккаунтов, их прогресс (звёзды, занятия, рекорды) и
 * таблица лидеров. Пароль хранится как SHA-256 хэш — так же, как в
 * самом приложении (см. Passwords в core).
 */
public final class Database {

    /** Пользователь — запись из таблицы users. */
    public record UserRow(long id, String name, String role,
                          int stars, int lessons, int bestQuiz, int bestMemory) {
    }

    private final Connection connection;

    public Database(String path) throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("PRAGMA journal_mode=WAL");
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE COLLATE NOCASE,
                        password_hash TEXT NOT NULL,
                        role TEXT NOT NULL,
                        stars INTEGER NOT NULL DEFAULT 0,
                        lessons INTEGER NOT NULL DEFAULT 0,
                        best_quiz INTEGER NOT NULL DEFAULT 0,
                        best_memory INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL DEFAULT (datetime('now'))
                    )
                    """);
        }
    }

    /** Создаёт пользователя; возвращает null, если имя уже занято. */
    public UserRow register(String name, String passwordHash, String role) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (name, password_hash, role) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : -1;
                return new UserRow(id, name.trim(), role, 0, 0, 0, 0);
            }
        } catch (SQLException e) {
            // SQLite: UNIQUE constraint violation (19)
            if (e.getErrorCode() == 19 || e.getMessage().toLowerCase().contains("unique")) return null;
            throw e;
        }
    }

    /** Ищет пользователя по имени и хэшу пароля; null — не найден. */
    public UserRow authenticate(String name, String passwordHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name, role, stars, lessons, best_quiz, best_memory"
                        + " FROM users WHERE name = ? COLLATE NOCASE AND password_hash = ?")) {
            ps.setString(1, name.trim());
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readUser(rs) : null;
            }
        }
    }

    public UserRow findById(long id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, name, role, stars, lessons, best_quiz, best_memory"
                        + " FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readUser(rs) : null;
            }
        }
    }

    /** Объединяет прогресс: сервер берёт максимум по каждой величине. */
    public UserRow updateProgress(long id, int stars, int lessons, int bestQuiz, int bestMemory)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET"
                        + " stars = MAX(stars, ?),"
                        + " lessons = MAX(lessons, ?),"
                        + " best_quiz = MAX(best_quiz, ?),"
                        + " best_memory = MAX(best_memory, ?)"
                        + " WHERE id = ?")) {
            ps.setInt(1, Math.max(0, stars));
            ps.setInt(2, Math.max(0, lessons));
            ps.setInt(3, Math.max(0, bestQuiz));
            ps.setInt(4, Math.max(0, bestMemory));
            ps.setLong(5, id);
            ps.executeUpdate();
        }
        return findById(id);
    }

    /** Таблица лидеров: сверху самые «звёздные» дети. */
    public List<UserRow> leaderboard(int limit) throws SQLException {
        List<UserRow> result = new ArrayList<>();
        String sql = "SELECT id, name, role, stars, lessons, best_quiz, best_memory"
                + " FROM users ORDER BY stars DESC, lessons DESC, name COLLATE NOCASE ASC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(100, limit)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(readUser(rs));
            }
        }
        return result;
    }

    private static UserRow readUser(ResultSet rs) throws SQLException {
        return new UserRow(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getInt("stars"),
                rs.getInt("lessons"),
                rs.getInt("best_quiz"),
                rs.getInt("best_memory"));
    }
}