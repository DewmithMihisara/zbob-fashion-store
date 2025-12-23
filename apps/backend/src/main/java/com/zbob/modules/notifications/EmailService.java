package com.zbob.modules.notifications;

import com.zbob.common.util.AfterCommit;
import com.zbob.config.MailProperties;
import com.zbob.modules.users.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds the emails the store sends. Each is sent only after the change it describes has been saved,
 * and a failed send is logged rather than undoing that change.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailSender sender;
    private final String storefrontUrl;

    public EmailService(EmailSender sender, MailProperties properties) {
        this.sender = sender;
        this.storefrontUrl = properties.storefrontUrl().replaceAll("/+$", "");
    }

    public void welcome(User user) {
        send(user.getEmail(), "Welcome to zbob", """
                Hi %s,

                Thanks for creating a zbob account. You can see your orders and saved addresses at any time:
                %s/account

                The zbob team
                """.formatted(user.getFullName(), storefrontUrl));
    }

    public void passwordReset(User user, String token) {
        send(user.getEmail(), "Reset your zbob password", """
                Hi %s,

                We received a request to reset your password. Use this link within the next hour:
                %s/reset-password?token=%s

                If you did not ask for this, you can ignore this email and your password will stay the same.

                The zbob team
                """.formatted(user.getFullName(), storefrontUrl, token));
    }

    public void passwordChanged(User user) {
        send(user.getEmail(), "Your zbob password was changed", """
                Hi %s,

                The password for your zbob account was just changed.

                If this was not you, reset your password straight away at %s/forgot-password and contact us.

                The zbob team
                """.formatted(user.getFullName(), storefrontUrl));
    }

    public void orderPlaced(User user, String orderNumber, int totalCents) {
        send(user.getEmail(), "Your zbob order " + orderNumber, """
                Hi %s,

                Thanks for your order %s. The total is %s.
                You can follow it at %s/account/orders

                The zbob team
                """.formatted(user.getFullName(), orderNumber, money(totalCents), storefrontUrl));
    }

    public void orderStatusChanged(User user, String orderNumber, String statusText) {
        send(user.getEmail(), "Your zbob order " + orderNumber + " is " + statusText, """
                Hi %s,

                Your order %s is now %s.
                You can see the details at %s/account/orders

                The zbob team
                """.formatted(user.getFullName(), orderNumber, statusText, storefrontUrl));
    }

    static String money(int cents) {
        return "$%d.%02d".formatted(cents / 100, cents % 100);
    }

    private void send(String to, String subject, String body) {
        EmailMessage message = new EmailMessage(to, subject, body);
        AfterCommit.run(() -> {
            try {
                sender.send(message);
            } catch (RuntimeException e) {
                log.warn("Could not send email '{}' to {}", subject, to, e);
            }
        });
    }
}
