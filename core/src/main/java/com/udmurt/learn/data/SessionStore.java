package com.udmurt.learn.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Помнит токен сервера для каждого локального аккаунта. */
public final class SessionStore {

    private static final String PREFS = "server-session";
    private static final String KEY_NAME = "name";
    private static final String KEY_TOKEN = "token";

    private SessionStore() {
    }

    public static void save(String name, String token) {
        try {
            Gdx.app.getPreferences(PREFS)
                    .putString(KEY_NAME, name == null ? "" : name)
                    .putString(KEY_TOKEN, token == null ? "" : token)
                    .flush();
        } catch (RuntimeException ignored) {
            // Нет доступа к настройкам — токен просто не запомнится.
        }
    }

    /** Возвращает сохранённый токен для имени, или null. */
    public static String tokenFor(String name) {
        if (name == null) return null;
        try {
            Preferences prefs = Gdx.app.getPreferences(PREFS);
            if (!name.equals(prefs.getString(KEY_NAME, ""))) return null;
            String token = prefs.getString(KEY_TOKEN, "");
            return token.isEmpty() ? null : token;
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}