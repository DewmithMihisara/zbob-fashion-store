package com.zbob.modules.notifications;

import com.zbob.config.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Sends through any SMTP service. Configure it with SPRING_MAIL_HOST, SPRING_MAIL_PORT, SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD. */
@Component
@ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mail;
    private final String from;

    public SmtpEmailSender(JavaMailSender mail, MailProperties properties) {
        this.mail = mail;
        this.from = properties.from();
    }

    @Override
    public void send(EmailMessage message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(message.to());
        email.setSubject(message.subject());
        email.setText(message.body());
        mail.send(email);
    }
}
