package com.udmurt.learn.lwjgl3;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * On macOS LWJGL3 must run on the first thread, so when the game is started
 * from a jar we relaunch the JVM with {@code -XstartOnFirstThread}. On every
 * other platform this class is a no-op.
 */
class StartupHelper {

    private static final String JVM_RESTARTED_ARG = "jvmIsRestarted";

    private StartupHelper() {
    }

    static boolean startNewJvmIfRequired() {
        return startNewJvmIfRequired(true);
    }

    static boolean startNewJvmIfRequired(boolean redirectOutput) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac")) {
            return false;
        }

        if (System.getProperty(JVM_RESTARTED_ARG) != null) {
            return false;
        }
        if (!startOnFirstThreadNotSet()) {
            return false;
        }

        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            if (javaPath.contains(" ")) {
                javaPath = "\"" + javaPath + "\"";
            }

            ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-XstartOnFirstThread",
                    "-DjvmIsRestarted=true");
            List<String> inputArguments = runtime.getInputArguments();
            for (String argument : inputArguments) {
                if (argument.startsWith("-XstartOnFirstThread")) continue;
                processBuilder.command().add(argument);
            }
            processBuilder.command().add("-cp");
            processBuilder.command().add(System.getProperty("java.class.path"));
            processBuilder.command().add(Lwjgl3Launcher.class.getName());
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);
            processBuilder.inheritIO().start().waitFor();

            System.exit(0);
            return true;
        } catch (Exception exception) {
            if (redirectOutput) {
                System.err.println("Failed to restart the JVM: " + exception.getMessage());
            }
            return false;
        }
    }

    private static boolean startOnFirstThreadNotSet() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        for (String argument : runtime.getInputArguments()) {
            if (argument.startsWith("-XstartOnFirstThread")) {
                return false;
            }
        }
        return true;
    }
}
