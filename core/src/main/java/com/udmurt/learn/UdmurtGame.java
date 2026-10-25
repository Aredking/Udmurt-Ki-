package com.udmurt.learn;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.JsonValue;
import com.udmurt.learn.assets.Assets;
import com.udmurt.learn.data.ServerApi;
import com.udmurt.learn.data.ServerConfig;
import com.udmurt.learn.data.SessionStore;
import com.udmurt.learn.data.UserStore;
import com.udmurt.learn.model.User;
import com.udmurt.learn.screen.LoginScreen;

/** Главный класс приложения. Работает и на ПК, и на Android, и на iOS. */
public class UdmurtGame extends Game {

    /** Клиент игрового сервера (облачная синхронизация). */
    public ServerApi server;

    /** Доступ к платформенным функциям (например, установка Tailscale). */
    public final Platform platform;

    public Assets assets;
    public UserStore users;

    private User currentUser;
    private String remoteToken;
    private String remoteName;

    public UdmurtGame() {
        this(Platform.NONE);
    }

    public UdmurtGame(Platform platform) {
        this.platform = platform == null ? Platform.NONE : platform;
    }

    @Override
    public void render() {
        try {
            super.render();
        } catch (Throwable t) {
            logCrash(t);
            if (t instanceof Error) throw (Error) t;
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            throw new RuntimeException(t);
        }
    }

    /** Пишет стек ошибки в crash.log (рядом с приложением) и в консоль. */
    public void logCrash(Throwable t) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Удмурт кыл — crash.log ===\n")
                    .append("Дата: ").append(new java.util.Date()).append('\n')
                    .append("Java: ").append(System.getProperty("java.version")).append('\n')
                    .append("ОС: ").append(System.getProperty("os.name"))
                    .append(' ').append(System.getProperty("os.version")).append('\n')
                    .append("\n---- Стек ошибки ----\n");
            appendTrace(sb, t);
            if (com.badlogic.gdx.Gdx.files != null) {
                com.badlogic.gdx.files.FileHandle out =
                        com.badlogic.gdx.Gdx.files.local("crash.log");
                out.writeString(sb.toString(), false, "UTF-8");
            }
            System.err.println(sb);
        } catch (Throwable ignored) {
        }
    }

    private static void appendTrace(StringBuilder sb, Throwable t) {
        sb.append(t).append('\n');
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append("    at ").append(element).append('\n');
        }
        Throwable cause = t.getCause();
        while (cause != null && cause != t) {
            sb.append("Caused by: ").append(cause).append('\n');
            for (StackTraceElement element : cause.getStackTrace()) {
                sb.append("    at ").append(element).append('\n');
            }
            cause = cause.getCause();
        }
    }

    /** Пишет шаг выполнения в trace.log — помогает найти место сбоя. */
    public void trace(String message) {
        System.out.println("[trace] " + message);
        try {
            if (com.badlogic.gdx.Gdx.files == null) return;
            com.badlogic.gdx.files.FileHandle out =
                    com.badlogic.gdx.Gdx.files.local("trace.log");
            out.writeString(new java.util.Date() + "  " + message + "\n", true, "UTF-8");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void create() {
        trace("create: begin");
        server = new ServerApi(ServerConfig.baseUrl());
        trace("create: server=" + ServerConfig.baseUrl());
        assets = new Assets();
        trace("create: assets ok");
        users = new UserStore();
        trace("create: users ok");
        setScreen(new LoginScreen(this));
        trace("create: login screen shown");
    }

    public User getUser() {
        return currentUser;
    }

    /** Пересоздаёт клиент сервера после смены адреса в настройках. */
    public void reloadServer() {
        this.server = new ServerApi(ServerConfig.baseUrl());
    }

    public void signIn(User user) {
        this.currentUser = user;
        this.remoteName = user == null ? null : user.name;
        this.remoteToken = SessionStore.tokenFor(user.name);
    }

    /** Вход с уже подтверждённым сервером токеном (аккаунт найден на сервере). */
    public void signInRemote(User user, String token) {
        this.currentUser = user;
        this.remoteName = user == null ? null : user.name;
        this.remoteToken = token;
        if (user != null && token != null) {
            SessionStore.save(user.name, token);
        }
    }

    public void clearUser() {
        this.currentUser = null;
        this.remoteToken = null;
        this.remoteName = null;
    }

    /**
     * Связывает локальный аккаунт с сервером: регистрирует его там при
     * необходимости и получает токен. Если сервер недоступен — приложение
     * продолжает работать в офлайн-режиме. Колбэк вызывается в любом случае.
     */
    public void syncWithServer(User user, String plainPassword, final Runnable done) {
        trace("sync: start user=" + (user == null ? "null" : user.name));
        if (user == null || plainPassword == null) {
            trace("sync: skip (null)");
            if (done != null) done.run();
            return;
        }
        if (SessionStore.tokenFor(user.name) != null) {
            trace("sync: token already exists");
            if (done != null) done.run();
            return;
        }
        final String role = user.role.name().toLowerCase();
        trace("sync: register " + user.name + " role=" + role);
        server.register(user.name, role, plainPassword, new ServerApi.Listener() {
            @Override
            public void onSuccess(JsonValue json) {
                trace("sync: register ok");
                adoptToken(user, json);
                if (done != null) done.run();
            }

            @Override
            public void onError(String message) {
                trace("sync: register err='" + message + "'");
                if (message.toLowerCase().contains("name already taken")) {
                    server.login(user.name, plainPassword, new ServerApi.Listener() {
                        @Override
                        public void onSuccess(JsonValue json) {
                            trace("sync: login ok");
                            adoptToken(user, json);
                            if (done != null) done.run();
                        }

                        @Override
                        public void onError(String loginMessage) {
                            trace("sync: login err='" + loginMessage + "'");
                            // Пароль на сервере не совпал: остаёмся в офлайн-режиме.
                            if (done != null) done.run();
                        }
                    });
                } else {
                    if (done != null) done.run();
                }
            }
        });
    }

    private void adoptToken(User user, JsonValue json) {
        if (json == null || !json.has("token")) return;
        String token = json.getString("token");
        if (token.isEmpty()) return;
        this.remoteToken = token;
        this.remoteName = user.name;
        SessionStore.save(user.name, token);
        pushProgress(user);
    }

    /** Обновляет локальные данные и, если есть токен, выгружает на сервер. */
    public void onUserUpdated(User user) {
        if (user == null) return;
        users.update(user);
        pushProgress(user);
    }

    /**
     * Выгружает текущий прогресс на сервер. Без токена (офлайн) ничего не делает,
     * поэтому рейтинг на сервере обновится при следующем успешном входе.
     */
    private void pushProgress(User user) {
        if (remoteToken == null || user == null) return;
        server.pushProgress(remoteToken, user, new ServerApi.Listener() {
            @Override
            public void onSuccess(JsonValue json) {
            }

            @Override
            public void onError(String message) {
                // Сервер недоступен: данные останутся локальными до следующей попытки.
            }
        });
    }

    /** Токен для запросов к серверу, или null, если облако недоступно. */
    public String remoteToken() {
        return remoteToken;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        assets.dispose();
        super.dispose();
    }
}
