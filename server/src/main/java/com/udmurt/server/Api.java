package com.udmurt.server;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * REST API сервера. Все сообщения — JSON в UTF-8.
 *
 * POST /api/register   {name, password, role}                  -> {token, user}
 * POST /api/login      {name, password}                        -> {token, user}
 * GET  /api/me         Authorization: Bearer <token>           -> {user}
 * POST /api/progress   Authorization: Bearer <token>
 *                      {stars, lessons, best_quiz, best_memory} -> {user}
 * GET  /api/leaderboard?limit=10                              -> {users:[...]}
 */
public final class Api {

    /** Пользователь в ответах API. */
    public static final class UserView {
        final long id;
        final String name;
        final String role;
        final int stars;
        final int lessons;
        final int bestQuiz;
        final int bestMemory;

        UserView(Database.UserRow row) {
            this.id = row.id();
            this.name = row.name();
            this.role = row.role();
            this.stars = row.stars();
            this.lessons = row.lessons();
            this.bestQuiz = row.bestQuiz();
            this.bestMemory = row.bestMemory();
        }
    }

    private static final class RegisterRequest {
        String name;
        String password;
        String role;
    }

    private static final class LoginRequest {
        String name;
        String password;
    }

    private static final class ProgressRequest {
        int stars;
        int lessons;
        @SerializedName("best_quiz")
        int bestQuiz;
        @SerializedName("best_memory")
        int bestMemory;
    }

    private static final class SessionResponse {
        final String token;
        final UserView user;

        SessionResponse(String token, UserView user) {
            this.token = token;
            this.user = user;
        }
    }

    private final Database db;
    private final Sessions sessions = new Sessions();
    private final Gson gson = new Gson();

    public Api(Database db) {
        this.db = db;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            switch (path) {
                case "/api/register" -> {
                    if (method.equals("POST")) register(exchange);
                    else send(exchange, 405, "{\"error\":\"method not allowed\"}");
                }
                case "/api/login" -> {
                    if (method.equals("POST")) login(exchange);
                    else send(exchange, 405, "{\"error\":\"method not allowed\"}");
                }
                case "/api/me" -> {
                    if (method.equals("GET")) me(exchange);
                    else send(exchange, 405, "{\"error\":\"method not allowed\"}");
                }
                case "/api/progress" -> {
                    if (method.equals("POST")) progress(exchange);
                    else send(exchange, 405, "{\"error\":\"method not allowed\"}");
                }
                case "/api/leaderboard" -> {
                    if (method.equals("GET")) leaderboard(exchange);
                    else send(exchange, 405, "{\"error\":\"method not allowed\"}");
                }
                default -> send(exchange, 404, "{\"error\":\"not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, "{\"error\":\"server error\"}");
        }
    }

    private void register(HttpExchange exchange) throws Exception {
        RegisterRequest request = gson.fromJson(body(exchange), RegisterRequest.class);
        if (request == null || request.name == null || request.password == null) {
            send(exchange, 400, "{\"error\":\"name and password are required\"}");
            return;
        }
        String name = request.name.trim();
        if (name.isEmpty() || name.length() > 60) {
            send(exchange, 400, "{\"error\":\"name must be 1-60 characters\"}");
            return;
        }
        if (request.password.length() < 4 || request.password.length() > 64) {
            send(exchange, 400, "{\"error\":\"password must be 4-64 characters\"}");
            return;
        }
        Database.UserRow row = db.register(name, sha256(request.password), normalizeRole(request.role));
        if (row == null) {
            send(exchange, 409, "{\"error\":\"name already taken\"}");
            return;
        }
        send(exchange, 200, gson.toJson(new SessionResponse(sessions.create(row.id()), new UserView(row))));
    }

    private void login(HttpExchange exchange) throws Exception {
        LoginRequest request = gson.fromJson(body(exchange), LoginRequest.class);
        if (request == null || request.name == null || request.password == null) {
            send(exchange, 400, "{\"error\":\"name and password are required\"}");
            return;
        }
        Database.UserRow row = db.authenticate(request.name, sha256(request.password));
        if (row == null) {
            send(exchange, 401, "{\"error\":\"wrong name or password\"}");
            return;
        }
        send(exchange, 200, gson.toJson(new SessionResponse(sessions.create(row.id()), new UserView(row))));
    }

    private void me(HttpExchange exchange) throws Exception {
        long userId = sessions.resolve(bearer(exchange));
        if (userId < 0) {
            send(exchange, 401, "{\"error\":\"unauthorized\"}");
            return;
        }
        Database.UserRow row = db.findById(userId);
        if (row == null) send(exchange, 404, "{\"error\":\"user not found\"}");
        else send(exchange, 200, gson.toJson(Map.of("user", new UserView(row))));
    }

    private void progress(HttpExchange exchange) throws Exception {
        long userId = sessions.resolve(bearer(exchange));
        if (userId < 0) {
            send(exchange, 401, "{\"error\":\"unauthorized\"}");
            return;
        }
        ProgressRequest request = gson.fromJson(body(exchange), ProgressRequest.class);
        if (request == null) {
            send(exchange, 400, "{\"error\":\"empty body\"}");
            return;
        }
        Database.UserRow row = db.updateProgress(
                userId, request.stars, request.lessons, request.bestQuiz, request.bestMemory);
        send(exchange, 200, gson.toJson(Map.of("user", new UserView(row))));
    }

    private void leaderboard(HttpExchange exchange) throws Exception {
        int limit = 10;
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("limit=")) {
            int start = query.indexOf("limit=") + 6;
            int end = query.indexOf('&', start);
            if (end < 0) end = query.length();
            try {
                limit = Integer.parseInt(query.substring(start, end));
            } catch (NumberFormatException ignored) {
            }
        }
        java.util.List<Database.UserRow> rows = db.leaderboard(limit);
        java.util.List<UserView> users = new java.util.ArrayList<>();
        for (Database.UserRow row : rows) users.add(new UserView(row));
        send(exchange, 200, gson.toJson(Map.of("users", users)));
    }

    // ------------------------------------------------------------------

    private static String normalizeRole(String role) {
        if (role == null) return "preschool";
        String lower = role.trim().toLowerCase();
        return lower.startsWith("school") ? "school" : "preschool";
    }

    /** SHA-256 в hex — точно так же хэширует пароли приложение (Passwords). */
    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }

    private static String bearer(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring("Bearer ".length()).trim();
    }

    private static String body(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        } finally {
            exchange.close();
        }
    }
}