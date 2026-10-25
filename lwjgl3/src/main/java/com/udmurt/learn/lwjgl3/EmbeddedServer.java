package com.udmurt.learn.lwjgl3;

import com.sun.net.httpserver.HttpServer;
import com.udmurt.server.Api;
import com.udmurt.server.Database;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Поднимает сервер прямо внутри приложения на фоновом потоке-демоне.
 *
 * Так игра работает «из коробки»: при её запуске сервер стартует вместе с ней,
 * но отдельного окна консоли не появляется. Если сервер уже запущен вручную
 * (порт занят), встроенный не поднимается — приложение подключается к нему.
 */
final class EmbeddedServer {

    private static final int PORT = 8080;
    private static final String DB_FILE = "udmurt_server.db";

    private static final AtomicBoolean started = new AtomicBoolean(false);

    private EmbeddedServer() {
    }

    static void start() {
        if (!started.compareAndSet(false, true)) return;
        if (isPortBusy(PORT)) return;

        Thread thread = new Thread(EmbeddedServer::launch, "udmurt-embedded-server");
        thread.setDaemon(true);
        thread.start();
    }

    private static void launch() {
        try {
            Database database = new Database(DB_FILE);
            Api api = new Api(database);

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/api", api::handle);

            ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
                Thread worker = new Thread(runnable, "udmurt-server-worker");
                worker.setDaemon(true);
                return worker;
            });
            server.setExecutor(executor);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1)));
            System.out.println("Встроенный сервер запущен: http://localhost:" + PORT + "/api");
        } catch (Throwable error) {
            System.out.println("Встроенный сервер не запущен: " + error.getMessage());
        }
    }

    private static boolean isPortBusy(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 300);
            return true;
        } catch (Exception notRunning) {
            return false;
        }
    }
}
