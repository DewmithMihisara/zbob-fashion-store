package com.zbob.modules.brands;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.brands.entity.Brand;
import com.zbob.modules.brands.repository.BrandRepository;
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
class BrandApiTest {

    private static final String ADMIN_URL = "/api/v1/admin/brands";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BrandRepository brands;

    @Test
    void anyoneCanList() throws Exception {
        brands.save(new Brand("zbob", "zbob"));

        mvc.perform(get("/api/v1/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("zbob"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void adminListIsPaged() throws Exception {
        brands.save(new Brand("Aaa", "aaa"));
        brands.save(new Brand("Bbb", "bbb"));

        mvc.perform(get(ADMIN_URL).param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.content[0].name").value("Bbb"))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotCreate() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Other\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCreatesAndRenames() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"North Line\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("north-line"));

        Brand brand = brands.save(new Brand("Old", "old"));
        mvc.perform(put(ADMIN_URL + "/" + brand.getId()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("new"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blankNameFailsValidation() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void onlyAdminDeletes() throws Exception {
        Brand brand = brands.save(new Brand("Old", "old"));

        mvc.perform(delete(ADMIN_URL + "/" + brand.getId())).andExpect(status().isForbidden());
    }
}
