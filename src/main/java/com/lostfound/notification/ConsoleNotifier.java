package com.lostfound.notification;

import com.lostfound.model.User;

public class ConsoleNotifier implements Notifier {
    @Override
    public void notify(User user, String message) {
        System.out.println("[Notification] To: " + user.getEmail() + " | " + message);
    }
}
