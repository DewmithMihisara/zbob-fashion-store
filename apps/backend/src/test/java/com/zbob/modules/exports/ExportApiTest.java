package com.zbob.modules.exports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.support.TestData;
import java.nio.charset.StandardCharsets;
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
class ExportApiTest {

    private static final String URL = "/api/v1/admin/exports/";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    private String body(String name) throws Exception {
        return mvc.perform(get(URL + name + ".csv")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffExportsStockWithAHeaderRow() throws Exception {
        data.variant("Export, Tee", 1000, 7);

        String csv = body("inventory");

        assertThat(csv).startsWith("﻿variant_id,product,sku,size,colour,in_stock,low_stock_at\r\n");
        assertThat(csv).contains("\"Export, Tee\"").contains(",M,Black,7,5");
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void downloadsWithADatedFileName() throws Exception {
        mvc.perform(get(URL + "products.csv"))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"zbob-products-")))
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminExportsCustomersButNotStaffAccounts() throws Exception {
        data.customer("export-me@zbob.test");

        String csv = body("customers");

        assertThat(csv).contains("export-me@zbob.test").doesNotContain("admin@zbob.test");
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanNotExportPersonalData() throws Exception {
        for (String name : new String[] {"customers", "orders", "subscribers"}) {
            mvc.perform(get(URL + name + ".csv")).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unknownExportIsNotFound() throws Exception {
        mvc.perform(get(URL + "passwords.csv")).andExpect(status().isNotFound());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get(URL + "products.csv")).andExpect(status().isUnauthorized());
    }
}
