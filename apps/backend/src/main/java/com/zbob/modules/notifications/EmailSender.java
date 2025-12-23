package com.zbob.modules.notifications;

/** Delivers an email. Which implementation runs is chosen by app.mail.provider. */
public interface EmailSender {

    void send(EmailMessage message);
}
