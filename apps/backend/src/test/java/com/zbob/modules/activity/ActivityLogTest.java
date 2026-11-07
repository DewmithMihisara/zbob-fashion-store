package com.zbob.modules.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.brands.repository.BrandRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Not transactional, because entries are saved in their own transaction. Cleans up its own rows. */
@SpringBootTest
@AutoConfigureMockMvc
class ActivityLogTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ActivityRepository entries;

    @Autowired
    private UserRepository users;

    @Autowired
    private JwtService jwt;

    @Autowired
    private BrandRepository brands;

    private User staff;
    private User admin;
    private long before;
    private String brandName;

    @BeforeEach
    void setUp() {
        long n = System.nanoTime();
        staff = users.save(new User("act-staff-" + n + "@zbob.test", "hash", "Act Staff", Role.STAFF));
        admin = users.save(new User("act-admin-" + n + "@zbob.test", "hash", "Act Admin", Role.ADMIN));
        before = entries.count();
        brandName = "Activity Brand " + n;
    }

    @AfterEach
    void tearDown() {
        entries.findAll().stream()
                .filter(e -> staff.getId().equals(e.getActorId()) || admin.getId().equals(e.getActorId())
                        || e.getActorName().contains("act-"))
                .forEach(entries::delete);
        users.deleteAll(List.of(staff, admin));
        brands.findAll().stream().filter(b -> b.getName().equals(brandName)).forEach(brands::delete);
    }

    private String bearer(User user) {
        return "Bearer " + jwt.issue(user.getId(), user.getRole());
    }

    private List<ActivityEntry> newEntries() {
        return entries.findAll().stream().skip(before).toList();
    }

    @Test
    void recordsAChangeWithTheActorsName() throws Exception {
        mvc.perform(post("/api/v1/admin/brands").header("Authorization", bearer(staff)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + brandName + "\"}"))
                .andExpect(status().isCreated());

        assertThat(newEntries()).anySatisfy(e -> {
            assertThat(e.getActorName()).startsWith("Act Staff <");
            assertThat(e.getSummary()).isEqualTo("Added brands");
            assertThat(e.getAction()).isEqualTo("CREATE");
        });
    }

    @Test
    void doesNotRecordReadsOrFailedChanges() throws Exception {
        mvc.perform(get("/api/v1/admin/products").header("Authorization", bearer(staff))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/brands").header("Authorization", bearer(staff)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}")).andExpect(status().isBadRequest());

        assertThat(newEntries()).noneMatch(e -> staff.getId().equals(e.getActorId()));
    }

    @Test
    void recordsExports() throws Exception {
        mvc.perform(get("/api/v1/admin/exports/products.csv").header("Authorization", bearer(staff))).andExpect(status().isOk());

        assertThat(newEntries()).anyMatch(e -> e.getSummary().equals("Exported products.csv"));
    }

    @Test
    void onlyAdminsCanReadTheLog() throws Exception {
        mvc.perform(get("/api/v1/admin/activity").header("Authorization", bearer(staff))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/activity").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void recordsFailedDashboardSignIns() throws Exception {
        String email = "act-nobody-" + System.nanoTime() + "@zbob.test";
        mvc.perform(post("/api/v1/auth/admin/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"wrong\"}".formatted(email))).andExpect(status().isUnauthorized());

        assertThat(newEntries()).anySatisfy(e -> {
            assertThat(e.getActorName()).isEqualTo(email);
            assertThat(e.getAction()).isEqualTo("SIGN_IN_FAILED");
        });
    }
}
