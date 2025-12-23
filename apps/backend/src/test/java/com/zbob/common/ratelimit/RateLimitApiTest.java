package com.zbob.common.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "app.rate-limit.login-per-email=3",
        "app.rate-limit.login-per-ip=5",
        "app.rate-limit.signup-per-ip=2",
        "app.rate-limit.forms-per-ip=2"})
@AutoConfigureMockMvc
@Transactional
class RateLimitApiTest {

    @Autowired
    private MockMvc mvc;

    private static RequestPostProcessor from(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private ResultActions adminLogin(String ip, String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/admin/login").with(from(ip)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
    }

    @Test
    void lockedEmailRefusesEvenTheRightPassword() throws Exception {
        for (int i = 0; i < 3; i++) {
            adminLogin("10.0.1.1", "admin@zbob.test", "wrong").andExpect(status().isUnauthorized());
        }

        adminLogin("10.0.1.2", "admin@zbob.test", "test-admin-password")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(jsonPath("$.message").value("Too many attempts. Please try again in 15 minutes."));
    }

    @Test
    void successClearsTheCount() throws Exception {
        String email = "nobody-a@zbob.test";
        adminLogin("10.0.2.1", email, "wrong");
        adminLogin("10.0.2.1", email, "wrong");
        adminLogin("10.0.2.1", "admin@zbob.test", "test-admin-password").andExpect(status().isOk());
        adminLogin("10.0.2.1", "admin@zbob.test", "wrong");
        adminLogin("10.0.2.1", "admin@zbob.test", "wrong");

        adminLogin("10.0.2.1", "admin@zbob.test", "test-admin-password").andExpect(status().isOk());
    }

    @Test
    void oneAddressCanNotTryManyEmails() throws Exception {
        for (int i = 0; i < 5; i++) {
            adminLogin("10.0.3.1", "spray-" + i + "@zbob.test", "wrong").andExpect(status().isUnauthorized());
        }

        adminLogin("10.0.3.1", "spray-9@zbob.test", "wrong").andExpect(status().isTooManyRequests());
        adminLogin("10.0.3.2", "spray-9@zbob.test", "wrong").andExpect(status().isUnauthorized());
    }

    @Test
    void emailCaseDoesNotDodgeTheLimit() throws Exception {
        for (String email : new String[] {"Case@zbob.test", "CASE@zbob.test", "case@zbob.test"}) {
            adminLogin("10.0.4.1", email, "wrong");
        }

        adminLogin("10.0.4.2", "cAsE@zbob.test", "wrong").andExpect(status().isTooManyRequests());
    }

    @Test
    void signUpsAreLimitedPerAddress() throws Exception {
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/auth/customer/register").with(from("10.0.5.1")).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"rl-%d@zbob.test\",\"password\":\"password123\",\"fullName\":\"R\"}".formatted(i)))
                    .andExpect(status().isCreated());
        }

        mvc.perform(post("/api/v1/auth/customer/register").with(from("10.0.5.1")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"rl-9@zbob.test\",\"password\":\"password123\",\"fullName\":\"R\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void contactAndNewsletterShareTheFormLimit() throws Exception {
        mvc.perform(post("/api/v1/newsletter").with(from("10.0.6.1")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"a@example.com\"}")).andExpect(status().isAccepted());
        mvc.perform(post("/api/v1/contact").with(from("10.0.6.1")).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"A\",\"email\":\"a@example.com\",\"message\":\"Hi\"}")).andExpect(status().isAccepted());

        mvc.perform(post("/api/v1/contact").with(from("10.0.6.1")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"A\",\"email\":\"a@example.com\",\"message\":\"Hi\"}"))
                .andExpect(status().isTooManyRequests());
    }
}
