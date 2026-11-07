package com.zbob.modules.customers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChangePasswordTest {

    private static final String EMAIL = "pw@zbob.test";
    private static final String OLD = "old-password";
    private static final String NEW = "new-password";

    @Autowired
    private MockMvc mvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        String body = mvc.perform(post("/api/v1/auth/customer/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\",\"fullName\":\"Pat\"}".formatted(EMAIL, OLD)))
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(body, "$.accessToken");
    }

    private ResultActions change(String current, String next) throws Exception {
        return mvc.perform(put("/api/v1/me/password").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}".formatted(current, next)));
    }

    private ResultActions login(String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/customer/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(EMAIL, password)));
    }

    @Test
    void newPasswordReplacesTheOldOne() throws Exception {
        change(OLD, NEW).andExpect(status().isNoContent());

        login(NEW).andExpect(status().isOk());
        login(OLD).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongCurrentPasswordChangesNothing() throws Exception {
        change("not-the-password", NEW).andExpect(status().isBadRequest());

        login(OLD).andExpect(status().isOk());
    }

    @Test
    void shortNewPasswordIsRejected() throws Exception {
        change(OLD, "short").andExpect(status().isBadRequest());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(put("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"a\",\"newPassword\":\"long-enough\"}"))
                .andExpect(status().isUnauthorized());
    }
}
