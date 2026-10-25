package com.udmurt.server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Точка входа сервера хранения базы данных приложения.
 *
 * Параметры (в порядке приоритета):
 *   - env PORT   или аргумент командной строки (по умолчанию 8080)
 *   - env DB_PATH или аргумент (по умолчанию udmurt_server.db)
 *
 * Пример запуска собранного fat-jar:
 *   java -jar server/build/libs/udmurt-server-all.jar
 *   java -jar server/build/libs/udmurt-server-all.jar 9000
 *   set PORT=9000 && java -jar server/build/libs/udmurt-server-all.jar
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(envOr("PORT", args, 0, "8080"));
        String dbPath = envOr("DB_PATH", args, 1, "udmurt_server.db");

        Database database = new Database(dbPath);
        Api api = new Api(database);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", api::handle);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Сервер «Удмурт Кыл» запущен:");
        System.out.println("  URL:     http://localhost:" + port + "/api");
        System.out.println("  БД:      " + dbPath + " (SQLite)");
        System.out.println("  Методы:  POST /api/register, POST /api/login,");
        System.out.println("           GET /api/me, POST /api/progress, GET /api/leaderboard");
    }

    private static String envOr(String envName, String[] args, int argIndex, String fallback) {
        String env = System.getenv(envName);
        if (env != null && !env.isBlank()) return env.trim();
        if (args.length > argIndex && args[argIndex] != null && !args[argIndex].isBlank()) {
            return args[argIndex].trim();
        }
        return fallback;
    }
}