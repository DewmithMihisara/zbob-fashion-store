package com.zbob.modules.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthControllerTest {

    private static final String ADMIN_LOGIN = "/api/v1/auth/admin/login";
    private static final String CUSTOMER_LOGIN = "/api/v1/auth/customer/login";
    private static final String REGISTER = "/api/v1/auth/customer/register";

    @Autowired
    private MockMvc mvc;

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String login(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private void registerCustomer(String email) throws Exception {
        postJson(REGISTER, "{\"email\":\"%s\",\"password\":\"password123\",\"fullName\":\"Test Person\"}".formatted(email))
                .andExpect(status().isCreated());
    }

    @Test
    void bootstrappedAdminCanLogIn() throws Exception {
        postJson(ADMIN_LOGIN, login("admin@zbob.test", "test-admin-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        postJson(ADMIN_LOGIN, login("admin@zbob.test", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void unknownEmailGetsTheSameError() throws Exception {
        postJson(ADMIN_LOGIN, login("nobody@zbob.test", "whatever"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void customerCannotUseAdminLogin() throws Exception {
        registerCustomer("shopper@zbob.test");

        postJson(ADMIN_LOGIN, login("shopper@zbob.test", "password123")).andExpect(status().isUnauthorized());
        postJson(CUSTOMER_LOGIN, login("shopper@zbob.test", "password123")).andExpect(status().isOk());
    }

    @Test
    void adminCannotUseCustomerLogin() throws Exception {
        postJson(CUSTOMER_LOGIN, login("admin@zbob.test", "test-admin-password")).andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsRejectedCaseInsensitively() throws Exception {
        registerCustomer("dup@zbob.test");

        postJson(REGISTER, "{\"email\":\"DUP@zbob.test\",\"password\":\"password123\",\"fullName\":\"Other\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationReportsFieldErrors() throws Exception {
        postJson(REGISTER, "{\"email\":\"not-an-email\",\"password\":\"short\",\"fullName\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.password", notNullValue()));
    }

    @Test
    void adminEndpointsRequireAuthentication() throws Exception {
        mvc.perform(get("/api/v1/admin/anything"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
