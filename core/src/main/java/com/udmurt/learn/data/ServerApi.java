package com.udmurt.learn.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.udmurt.learn.model.User;

/**
 * Клиент игрового сервера. Работает на libGDX {@code Net}, чтобы не тянуть
 * лишние зависимости и одинаково работать на ПК, Android и iOS.
 * Все запросы асинхронные, ответы приходят в потоке отрисовки.
 */
public class ServerApi {

    public interface Listener {
        void onSuccess(JsonValue json);

        void onError(String message);
    }

    private static final int TIMEOUT_MS = 6000;

    private final String baseUrl;

    public ServerApi(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public void register(String name, String role, String password, Listener listener) {
        send(HttpMethods.POST, "/api/register",
                "{\"name\":\"" + esc(name) + "\",\"password\":\"" + esc(password)
                        + "\",\"role\":\"" + esc(role) + "\"}",
                listener);
    }

    public void login(String name, String password, Listener listener) {
        send(HttpMethods.POST, "/api/login",
                "{\"name\":\"" + esc(name) + "\",\"password\":\"" + esc(password) + "\"}",
                listener);
    }

    public void pushProgress(String token, User user, Listener listener) {
        HttpRequest request = request(HttpMethods.POST, "/api/progress",
                "{\"stars\":" + user.stars
                        + ",\"lessons\":" + user.lessons
                        + ",\"best_quiz\":" + user.bestQuiz
                        + ",\"best_memory\":" + user.bestMemory + "}");
        request.setHeader("Authorization", "Bearer " + token);
        dispatch(request, listener);
    }

    public void leaderboard(int limit, Listener listener) {
        send(HttpMethods.GET, "/api/leaderboard?limit=" + limit, null, listener);
    }

    private void send(String method, String path, String body, Listener listener) {
        dispatch(request(method, path, body), listener);
    }

    private HttpRequest request(String method, String path, String body) {
        HttpRequest request = new HttpRequest(method);
        request.setUrl(baseUrl + path);
        request.setTimeOut(TIMEOUT_MS);
        if (body != null) {
            request.setHeader("Content-Type", "application/json; charset=utf-8");
            request.setContent(body);
        }
        request.setHeader("Accept", "application/json");
        return request;
    }

    private void dispatch(HttpRequest request, final Listener listener) {
        try {
            if (Gdx.net == null) {
                deliverError(listener, "Сервер недоступен");
                return;
            }
            Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse response) {
                int status = response.getStatus().getStatusCode();
                String body = response.getResultAsString();
                JsonValue json = null;
                try {
                    if (body != null && !body.isBlank()) {
                        json = new JsonReader().parse(body);
                    }
                } catch (Exception ignored) {
                }
                if (status >= 200 && status < 300) {
                    deliverSuccess(listener, json);
                } else {
                    String message = json != null && json.has("error")
                            ? json.getString("error") : "Ошибка сервера (" + status + ")";
                    deliverError(listener, message);
                }
            }

            @Override
            public void failed(Throwable t) {
                deliverError(listener, "Сервер недоступен");
            }

            @Override
            public void cancelled() {
                deliverError(listener, "Отменено");
            }
            });
        } catch (Throwable t) {
            deliverError(listener, "Сервер недоступен");
        }
    }

    /**
     * libGDX вызывает ответы {@code Net} в фоновом потоке. Любой колбэк может
     * трогать графику (например, открыть новое меню), поэтому переносим все
     * ответы в поток отрисовки.
     */
    private static void deliverSuccess(final Listener listener, final JsonValue json) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess(json);
            }
        });
    }

    private static void deliverError(final Listener listener, final String message) {
        runOnMain(new Runnable() {
            @Override
            public void run() {
                listener.onError(message);
            }
        });
    }

    private static void runOnMain(Runnable runnable) {
        if (Gdx.app == null) {
            runnable.run();
        } else {
            Gdx.app.postRunnable(runnable);
        }
    }

    private static String esc(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}