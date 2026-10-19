package com.lostfound.notification;

public class NotifierFactory {
    public static Notifier getNotifier(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Notifier type must be provided.");
        }
        return switch (type.toUpperCase()) {
            case "CONSOLE" -> new ConsoleNotifier();
            case "EMAIL" -> new EmailNotifier();
            default -> throw new IllegalArgumentException("Unknown notifier type: " + type);
        };
    }
}
