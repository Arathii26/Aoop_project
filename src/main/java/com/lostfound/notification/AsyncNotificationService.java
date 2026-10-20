package com.lostfound.notification;

import com.lostfound.model.User;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;

public class AsyncNotificationService {
    private final ExecutorService executorService;
    private final String logPath;

    public AsyncNotificationService() {
        this(Executors.newFixedThreadPool(4), "logs/notifications.log");
    }

    public AsyncNotificationService(ExecutorService executorService, String logPath) {
        this.executorService = executorService;
        this.logPath = logPath;
    }

    public Future<Boolean> sendAsync(User user, String message, Notifier notifier) {
        Callable<Boolean> task = () -> {
            try {
                notifier.notify(user, message);
                writeLog(user, message);
                return true;
            } catch (Exception e) {
                writeLog(user, "FAILED: " + message + " | " + e.getMessage());
                throw e;
            }
        };
        return executorService.submit(task);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void writeLog(User user, String message) throws IOException {
        java.io.File file = new java.io.File(logPath);
        java.io.File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(java.time.LocalDateTime.now() + " | " + user.getEmail() + " | " + message + System.lineSeparator());
        }
    }
}
