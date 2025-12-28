package com.zbob.support;

import com.zbob.modules.notifications.EmailMessage;
import com.zbob.modules.notifications.EmailSender;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Keeps sent emails in memory so tests can read them. */
public class RecordingEmailSender implements EmailSender {

    private final List<EmailMessage> sent = new CopyOnWriteArrayList<>();

    @Override
    public void send(EmailMessage message) {
        sent.add(message);
    }

    public List<EmailMessage> to(String email) {
        return sent.stream().filter(m -> m.to().equals(email)).toList();
    }

    public Optional<EmailMessage> last(String email) {
        List<EmailMessage> messages = to(email);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.get(messages.size() - 1));
    }

    @TestConfiguration
    public static class Config {

        @Bean
        @Primary
        RecordingEmailSender recordingEmailSender() {
            return new RecordingEmailSender();
        }
    }
}
