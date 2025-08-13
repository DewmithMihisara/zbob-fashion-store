package com.zbob.modules.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.contact.entity.ContactMessage;
import com.zbob.modules.contact.repository.ContactMessageRepository;
import com.zbob.modules.contact.repository.NewsletterSubscriberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContactApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContactMessageRepository messages;

    @Autowired
    private NewsletterSubscriberRepository subscribers;

    @Test
    void anonymousCanSendMessage() throws Exception {
        mvc.perform(post("/api/v1/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sam\",\"email\":\"sam@example.com\",\"message\":\"Hello\"}"))
                .andExpect(status().isAccepted());

        assertThat(messages.count()).isEqualTo(1);
    }

    @Test
    void honeypotSubmissionIsAcceptedButNotSaved() throws Exception {
        mvc.perform(post("/api/v1/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bot\",\"email\":\"bot@example.com\",\"message\":\"Spam\",\"website\":\"http://spam\"}"))
                .andExpect(status().isAccepted());

        assertThat(messages.count()).isZero();
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        mvc.perform(post("/api/v1/contact").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sam\",\"email\":\"nope\",\"message\":\"Hello\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void subscribingTwiceKeepsOneRowAndStillSucceeds() throws Exception {
        for (String email : new String[] {"a@example.com", "A@example.com"}) {
            mvc.perform(post("/api/v1/newsletter").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"%s\"}".formatted(email)))
                    .andExpect(status().isAccepted());
        }

        assertThat(subscribers.count()).isEqualTo(1);
    }

    @Test
    void anonymousCannotReadMessages() throws Exception {
        mvc.perform(get("/api/v1/admin/contact-messages")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffReadsAndHandlesMessages() throws Exception {
        ContactMessage saved = messages.save(new ContactMessage("Sam", "sam@example.com", null, "Hello"));

        mvc.perform(get("/api/v1/admin/contact-messages")).andExpect(jsonPath("$.content", hasSize(1)));
        mvc.perform(patch("/api/v1/admin/contact-messages/" + saved.getId()).param("handled", "true"))
                .andExpect(jsonPath("$.handled").value(true));
    }
}
