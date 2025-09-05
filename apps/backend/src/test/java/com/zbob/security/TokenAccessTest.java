package com.zbob.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TokenAccessTest {

    private static final String ADMIN_ENDPOINT = "/api/v1/admin/products";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwt;

    private User save(String email, Role role) {
        return users.save(new User(email, "hash", "Person", role));
    }

    private String bearer(User user) {
        return "Bearer " + jwt.issue(user.getId(), user.getRole());
    }

    @Test
    void staffTokenReachesAdminApi() throws Exception {
        User staff = save("staff@zbob.test", Role.STAFF);

        mvc.perform(get(ADMIN_ENDPOINT).header("Authorization", bearer(staff))).andExpect(status().isOk());
    }

    @Test
    void customerTokenIsForbidden() throws Exception {
        User customer = save("customer@zbob.test", Role.CUSTOMER);

        mvc.perform(get(ADMIN_ENDPOINT).header("Authorization", bearer(customer))).andExpect(status().isForbidden());
    }

    @Test
    void disabledAccountLosesAccessImmediately() throws Exception {
        User staff = save("gone@zbob.test", Role.STAFF);
        String token = bearer(staff);
        staff.setActive(false);
        users.flush();

        mvc.perform(get(ADMIN_ENDPOINT).header("Authorization", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void demotedAccountLosesAccessImmediately() throws Exception {
        User staff = save("demoted@zbob.test", Role.STAFF);
        String token = bearer(staff);
        staff.setRole(Role.CUSTOMER);
        users.flush();

        mvc.perform(get(ADMIN_ENDPOINT).header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    void garbageTokenIsUnauthorized() throws Exception {
        mvc.perform(get(ADMIN_ENDPOINT).header("Authorization", "Bearer nonsense")).andExpect(status().isUnauthorized());
    }
}
