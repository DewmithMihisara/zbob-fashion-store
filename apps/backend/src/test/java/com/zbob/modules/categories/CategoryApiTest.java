package com.zbob.modules.categories;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.repository.CategoryRepository;
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
class CategoryApiTest {

    private static final String ADMIN_URL = "/api/v1/admin/categories";
    private static final String BODY = "{\"name\":\"Men's Wear\",\"position\":1}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CategoryRepository categories;

    @Test
    void anyoneCanListCategories() throws Exception {
        categories.save(new Category("Outerwear", "outerwear"));

        mvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("outerwear"));
    }

    @Test
    void anonymousCannotCreate() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotCreate() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCreatesWithGeneratedSlug() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("mens-wear"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void duplicateNameConflicts() throws Exception {
        categories.save(new Category("Men's Wear", "mens-wear"));

        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotDelete() throws Exception {
        Category category = categories.save(new Category("Outerwear", "outerwear"));

        mvc.perform(delete(ADMIN_URL + "/" + category.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeletes() throws Exception {
        Category category = categories.save(new Category("Outerwear", "outerwear"));

        mvc.perform(delete(ADMIN_URL + "/" + category.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletingUnknownCategoryIsNotFound() throws Exception {
        mvc.perform(delete(ADMIN_URL + "/999999")).andExpect(status().isNotFound());
    }
}
