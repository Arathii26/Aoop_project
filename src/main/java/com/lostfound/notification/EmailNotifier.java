package com.lostfound.notification;

import com.lostfound.model.User;

public class EmailNotifier implements Notifier {
    private final MailSender mailSender;

    public EmailNotifier() {
        this(new MockMailSender());
    }

    public EmailNotifier(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void notify(User user, String message) {
        mailSender.send(user.getEmail(), message);
    }

    public interface MailSender {
        void send(String email, String message);
    }

    public static class MockMailSender implements MailSender {
        @Override
        public void send(String email, String message) {
            System.out.println("[Email] To: " + email + " | " + message);
        }
    }
}
