package com.udmurt.learn.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/** Адрес игрового сервера. Настраивается, запоминается между запусками. */
public final class ServerConfig {

    private static final String PREFS = "server-settings";
    private static final String KEY_URL = "server_url_v2";

    /** Адрес ПК с сервером в сети Tailscale (для телефона). Меняется на экране «Сервер». */
    private static final String DEFAULT_ANDROID = "http://100.94.94.121:8080";
    private static final String DEFAULT_DESKTOP = "http://localhost:8080";

    private ServerConfig() {
    }

    /** Текущий адрес сервера, или null, если пользователь его ещё не задавал. */
    public static String savedBaseUrl() {
        Preferences prefs = prefs();
        if (prefs == null) return null;
        String url = prefs.getString(KEY_URL, null);
        return url == null || url.trim().isEmpty() ? null : url.trim();
    }

    public static String baseUrl() {
        String saved = savedBaseUrl();
        if (saved != null) return saved;
        return isAndroid() ? DEFAULT_ANDROID : DEFAULT_DESKTOP;
    }

    public static void setBaseUrl(String url) {
        Preferences prefs = prefs();
        if (prefs == null) return;
        prefs.putString(KEY_URL, url == null ? "" : url.trim());
        prefs.flush();
    }

    private static boolean isAndroid() {
        return Gdx.app != null
                && Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android;
    }

    private static Preferences prefs() {
        return Gdx.app == null ? null : Gdx.app.getPreferences(PREFS);
    }
}
