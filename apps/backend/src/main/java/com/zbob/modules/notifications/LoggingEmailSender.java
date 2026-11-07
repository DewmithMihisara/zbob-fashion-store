package com.zbob.modules.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Writes emails to the log instead of sending them, for development and until an email provider is set
 * up. The log then contains password reset links, so do not use this in production.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    public LoggingEmailSender() {
        log.warn("Emails are being written to this log, not sent. Set MAIL_PROVIDER=smtp to send them.");
    }

    @Override
    public void send(EmailMessage message) {
        log.info("Email to {} | {}\n{}", message.to(), message.subject(), message.body());
    }
}
