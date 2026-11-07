package com.zbob.modules.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.zbob.config.MailProperties;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmailServiceTest {

    private final List<EmailMessage> sent = new ArrayList<>();
    private final User user = new User("sam@example.com", "hash", "Sam", Role.CUSTOMER);

    private EmailService service(EmailSender sender) {
        return new EmailService(sender, new MailProperties("log", "zbob <hello@zbob.example>", "https://store.example.com/"));
    }

    @Test
    void resetEmailLinksToTheStorefront() {
        service(sent::add).passwordReset(user, "abc123");

        assertThat(sent).singleElement().satisfies(message -> {
            assertThat(message.to()).isEqualTo("sam@example.com");
            assertThat(message.subject()).isEqualTo("Reset your zbob password");
            assertThat(message.body()).contains("https://store.example.com/reset-password?token=abc123").contains("Hi Sam");
        });
    }

    @Test
    void orderEmailShowsTheTotal() {
        service(sent::add).orderPlaced(user, "ZB-1", 6505);

        assertThat(sent.get(0).body()).contains("ZB-1").contains("$65.05");
    }

    @Test
    void formatsMoney() {
        assertThat(EmailService.money(0)).isEqualTo("$0.00");
        assertThat(EmailService.money(1999)).isEqualTo("$19.99");
    }

    @Test
    void aFailedSendIsLoggedNotThrown() {
        EmailSender broken = message -> {
            throw new IllegalStateException("smtp down");
        };

        assertThatCode(() -> service(broken).welcome(user)).doesNotThrowAnyException();
    }
}
