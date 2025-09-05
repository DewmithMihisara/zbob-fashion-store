package com.zbob.modules.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.repository.UserRepository;
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
@WithMockUser(roles = "ADMIN")
class StaffApiTest {

    private static final String URL = "/api/v1/admin/staff";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    private static String createJson(String email, String role) {
        return "{\"email\":\"%s\",\"password\":\"password123\",\"fullName\":\"Person\",\"role\":\"%s\"}".formatted(email, role);
    }

    @Test
    void adminCreatesStaffWithoutExposingPasswords() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(createJson("new@zbob.test", "STAFF")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void customerRoleIsRefused() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(createJson("c@zbob.test", "CUSTOMER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotManageStaff() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
    }

    @Test
    void lastActiveAdminCannotBeDeactivatedOrDemoted() throws Exception {
        Long adminId = users.findByEmailIgnoreCase("admin@zbob.test").orElseThrow().getId();
        long otherAdmins = users.countByRoleAndActiveTrue(Role.ADMIN) - 1;
        org.assertj.core.api.Assertions.assertThat(otherAdmins).isZero();

        mvc.perform(put(URL + "/" + adminId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"A\",\"role\":\"ADMIN\",\"active\":false}"))
                .andExpect(status().isConflict());
        mvc.perform(put(URL + "/" + adminId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"A\",\"role\":\"STAFF\",\"active\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        Long adminId = users.findByEmailIgnoreCase("admin@zbob.test").orElseThrow().getId();

        mvc.perform(put(URL + "/" + adminId + "/password").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}
