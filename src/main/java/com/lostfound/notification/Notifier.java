package com.lostfound.notification;

import com.lostfound.model.User;

public interface Notifier {
    void notify(User user, String message);
}
