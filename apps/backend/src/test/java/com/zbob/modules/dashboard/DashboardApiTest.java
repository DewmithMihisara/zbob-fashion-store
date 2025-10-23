package com.zbob.modules.dashboard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
class DashboardApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Test
    @WithMockUser(roles = "STAFF")
    void countsStockProblems() throws Exception {
        data.variant("Empty Tee", 3000, 0);
        data.variant("Low Tee", 3000, 2);
        data.variant("Full Tee", 3000, 50);

        mvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").value(3))
                .andExpect(jsonPath("$.lowStockVariants").value(2))
                .andExpect(jsonPath("$.outOfStockVariants").value(1));
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get("/api/v1/admin/dashboard")).andExpect(status().isUnauthorized());
    }
}
