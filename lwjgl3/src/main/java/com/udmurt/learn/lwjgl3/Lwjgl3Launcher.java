package com.udmurt.learn.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.udmurt.learn.UdmurtGame;

/** Desktop (Windows / Linux / macOS) entry point. */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        installCrashHandler();
        if (StartupHelper.startNewJvmIfRequired()) return;
        scheduleSmokeExit();
        try {
            createApplication();
        } catch (Throwable t) {
            writeCrash(t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    /** Любое исключение пишется в файл crash.log рядом с приложением. */
    private static void installCrashHandler() {
        try {
            Thread.setDefaultUncaughtExceptionHandler((thread, t) -> {
                writeCrash(t);
                t.printStackTrace();
                System.exit(1);
            });
        } catch (SecurityException ignored) {
        }
    }

    private static void writeCrash(Throwable t) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Удмурт кыл — crash.log ===").append('\n')
                    .append("Дата: ").append(new java.util.Date()).append('\n')
                    .append("Версия Java: ").append(System.getProperty("java.version")).append('\n')
                    .append("ОС: ").append(System.getProperty("os.name"))
                    .append(' ').append(System.getProperty("os.version")).append('\n')
                    .append("Рабочая папка: ").append(System.getProperty("user.dir")).append('\n')
                    .append("Видеокарта (GL): ").append(glInfo()).append('\n')
                    .append('\n').append("---- Стек ошибки ----\n");
            for (StackTraceElement element : t.getStackTrace()) {
                sb.append("    at ").append(element).append('\n');
            }
            Throwable cause = t.getCause();
            while (cause != null && cause != t) {
                sb.append("    Caused by: ").append(cause).append('\n');
                for (StackTraceElement element : cause.getStackTrace()) {
                    sb.append("        at ").append(element).append('\n');
                }
                cause = cause.getCause();
            }
            String text = sb.toString();
            java.util.List<String> targets = new java.util.ArrayList<>();
            targets.add("crash.log");
            String home = System.getProperty("user.home");
            if (home != null && !home.isEmpty()) {
                targets.add(home + java.io.File.separator + "udmurt-crash.log");
            }
            for (String target : targets) {
                try {
                    java.nio.file.Files.write(java.nio.file.Paths.get(target),
                            text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String glInfo() {
        try {
            return Gdx.gl != null
                    ? Gdx.gl.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER) : "нет GL";
        } catch (Throwable ignored) {
            return "нет доступа к GL";
        }
    }

    /**
     * Необязательный авто-выход для проверок: если задан параметр
     * -Dudmurt.smokeMillis=4000, приложение закроется через указанное время.
     */
    private static void scheduleSmokeExit() {
        final long millis = Long.getLong("udmurt.smokeMillis", 0L);
        if (millis <= 0) return;
        Thread thread = new Thread(() -> {
            long deadline = System.currentTimeMillis() + millis;
            while (System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return;
                }
            }
            if (Gdx.app != null) Gdx.app.exit();
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static Lwjgl3Application createApplication() {
        EmbeddedServer.start();
        return new Lwjgl3Application(new UdmurtGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Удмурт кыл — учим язык");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(600, 900);
        configuration.setWindowSizeLimits(420, 640, -1, -1);
        configuration.setResizable(true);
        return configuration;
    }
}
