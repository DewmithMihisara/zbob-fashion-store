package com.zbob.modules.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StaffDeleteTest {

    private static final String URL = "/api/v1/admin/staff/";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwt;

    private User admin;
    private User staff;

    @BeforeEach
    void setUp() {
        admin = users.save(new User("boss@zbob.test", "hash", "Boss", Role.ADMIN));
        staff = users.save(new User("helper@zbob.test", "hash", "Helper", Role.STAFF));
    }

    private String bearer(User user) {
        return "Bearer " + jwt.issue(user.getId(), user.getRole());
    }

    @Test
    void adminDeletesStaff() throws Exception {
        mvc.perform(delete(URL + staff.getId()).header("Authorization", bearer(admin))).andExpect(status().isNoContent());

        assertThat(users.findById(staff.getId())).isEmpty();
    }

    @Test
    void cannotDeleteYourOwnAccount() throws Exception {
        mvc.perform(delete(URL + admin.getId()).header("Authorization", bearer(admin))).andExpect(status().isConflict());
    }

    @Test
    void staffCannotDeleteAccounts() throws Exception {
        mvc.perform(delete(URL + admin.getId()).header("Authorization", bearer(staff))).andExpect(status().isForbidden());
    }

    @Test
    void customersCanNotBeDeletedThroughStaffEndpoint() throws Exception {
        User customer = users.save(new User("c@zbob.test", "hash", "Customer", Role.CUSTOMER));

        mvc.perform(delete(URL + customer.getId()).header("Authorization", bearer(admin))).andExpect(status().isNotFound());
    }
}
