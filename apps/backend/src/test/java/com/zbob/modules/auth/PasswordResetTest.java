package com.zbob.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import com.zbob.support.RecordingEmailSender;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Not transactional: emails go out only after a commit, so these tests commit and clean up after themselves. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(RecordingEmailSender.Config.class)
class PasswordResetTest {

    private static final Pattern LINK = Pattern.compile("reset-password\\?token=([A-Za-z0-9_-]+)");
    private static final String PASSWORD = "first-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RecordingEmailSender emails;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwt;

    private String email;

    @BeforeEach
    void setUp() throws Exception {
        email = "reset-" + System.nanoTime() + "@zbob.test";
        mvc.perform(post("/api/v1/auth/customer/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"Riley\"}".formatted(email, PASSWORD)))
                .andExpect(status().isCreated());
    }

    @AfterEach
    void tearDown() {
        users.findByEmailIgnoreCase(email).ifPresent(users::delete);
    }

    private ResultActions requestReset(String address) throws Exception {
        return mvc.perform(post("/api/v1/auth/customer/password-reset").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\"}".formatted(address)));
    }

    private ResultActions confirm(String token, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/customer/password-reset/confirm").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"%s\",\"newPassword\":\"%s\"}".formatted(token, password)));
    }

    private ResultActions login(String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/customer/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
    }

    private String latestToken() {
        String body = emails.last(email).orElseThrow().body();
        Matcher matcher = LINK.matcher(body);
        assertThat(matcher.find()).as("reset link in email").isTrue();
        return matcher.group(1);
    }

    @Test
    void signUpSendsAWelcomeEmail() {
        assertThat(emails.to(email)).extracting(m -> m.subject()).contains("Welcome to zbob");
    }

    @Test
    void resetLinkSetsANewPasswordOnce() throws Exception {
        requestReset(email).andExpect(status().isAccepted());
        String token = latestToken();

        confirm(token, "second-password").andExpect(status().isNoContent());

        login("second-password").andExpect(status().isOk());
        login(PASSWORD).andExpect(status().isUnauthorized());
        confirm(token, "third-password").andExpect(status().isBadRequest());
        assertThat(emails.last(email).orElseThrow().subject()).isEqualTo("Your zbob password was changed");
    }

    @Test
    void onlyTheNewestLinkWorks() throws Exception {
        requestReset(email);
        String first = latestToken();
        requestReset(email);
        String second = latestToken();

        confirm(first, "second-password").andExpect(status().isBadRequest());
        confirm(second, "second-password").andExpect(status().isNoContent());
    }

    @Test
    void unknownEmailGetsTheSameAnswerAndNoEmail() throws Exception {
        String unknown = "nobody-" + System.nanoTime() + "@zbob.test";

        requestReset(unknown).andExpect(status().isAccepted());

        assertThat(emails.to(unknown)).isEmpty();
    }

    @Test
    void staffAccountsCanNotResetThroughTheStorefront() throws Exception {
        requestReset("admin@zbob.test").andExpect(status().isAccepted());

        assertThat(emails.to("admin@zbob.test")).isEmpty();
    }

    @Test
    void madeUpTokenIsRejected() throws Exception {
        confirm("not-a-real-token", "second-password")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This reset link is invalid or has expired. Please ask for a new one."));
    }

    @Test
    void repeatedRequestsForOneEmailAreQuietlyCapped() throws Exception {
        for (int i = 0; i < 5; i++) {
            requestReset(email).andExpect(status().isAccepted());
        }

        assertThat(emails.to(email).stream().filter(m -> m.subject().startsWith("Reset"))).hasSize(3);
    }

    @Test
    void changingPasswordInTheAccountRetiresOpenLinks() throws Exception {
        requestReset(email);
        String token = latestToken();
        String customerToken = JsonPath.read(login(PASSWORD).andReturn().getResponse().getContentAsString(), "$.accessToken");

        mvc.perform(put("/api/v1/me/password").header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"%s\",\"newPassword\":\"changed-password\"}".formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        confirm(token, "sneaky-password").andExpect(status().isBadRequest());
    }

    @Test
    void staffCanSendAResetLinkToACustomer() throws Exception {
        User customer = users.findByEmailIgnoreCase(email).orElseThrow();
        User staff = users.save(new User("staff-" + System.nanoTime() + "@zbob.test", "hash", "Staff", Role.STAFF));
        try {
            mvc.perform(post("/api/v1/admin/customers/" + customer.getId() + "/password-reset")
                            .header("Authorization", "Bearer " + jwt.issue(staff.getId(), staff.getRole())))
                    .andExpect(status().isAccepted());

            assertThat(latestToken()).isNotBlank();
        } finally {
            users.delete(staff);
        }
    }
}
