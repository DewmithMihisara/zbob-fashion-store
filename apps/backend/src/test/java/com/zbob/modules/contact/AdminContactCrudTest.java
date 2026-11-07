package com.zbob.modules.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.contact.entity.ContactMessage;
import com.zbob.modules.contact.entity.NewsletterSubscriber;
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
@WithMockUser(roles = "STAFF")
class AdminContactCrudTest {

    private static final String MESSAGES = "/api/v1/admin/contact-messages";
    private static final String SUBSCRIBERS = "/api/v1/admin/newsletter-subscribers";
    private static final String MESSAGE_JSON = "{\"name\":\"Sam\",\"email\":\"sam@example.com\",\"message\":\"By phone\",\"handled\":true}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContactMessageRepository messages;

    @Autowired
    private NewsletterSubscriberRepository subscribers;

    @Test
    void createsAndEditsMessages() throws Exception {
        mvc.perform(post(MESSAGES).contentType(MediaType.APPLICATION_JSON).content(MESSAGE_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handled").value(true));

        ContactMessage saved = messages.save(new ContactMessage("Old", "old@example.com", null, "Old text"));
        mvc.perform(put(MESSAGES + "/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(MESSAGE_JSON))
                .andExpect(jsonPath("$.name").value("Sam"))
                .andExpect(jsonPath("$.message").value("By phone"));
    }

    @Test
    void createsEditsAndDeletesSubscribers() throws Exception {
        mvc.perform(post(SUBSCRIBERS).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"A@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("a@example.com"));

        NewsletterSubscriber saved = subscribers.save(new NewsletterSubscriber("b@example.com"));
        mvc.perform(put(SUBSCRIBERS + "/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"c@example.com\"}"))
                .andExpect(jsonPath("$.email").value("c@example.com"));

        mvc.perform(delete(SUBSCRIBERS + "/" + saved.getId())).andExpect(status().isNoContent());
        assertThat(subscribers.existsByEmail("c@example.com")).isFalse();
    }

    @Test
    void refusesDuplicateSubscriberEmails() throws Exception {
        subscribers.save(new NewsletterSubscriber("a@example.com"));
        NewsletterSubscriber other = subscribers.save(new NewsletterSubscriber("b@example.com"));

        mvc.perform(post(SUBSCRIBERS).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.com\"}"))
                .andExpect(status().isConflict());
        mvc.perform(put(SUBSCRIBERS + "/" + other.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.com\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void subscriberListIsPaged() throws Exception {
        subscribers.save(new NewsletterSubscriber("a@example.com"));
        subscribers.save(new NewsletterSubscriber("b@example.com"));

        mvc.perform(get(SUBSCRIBERS).param("size", "1")).andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCanNotManageEither() throws Exception {
        mvc.perform(post(SUBSCRIBERS).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@example.com\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post(MESSAGES).contentType(MediaType.APPLICATION_JSON).content(MESSAGE_JSON))
                .andExpect(status().isForbidden());
    }
}
