package com.zbob.modules.blog;

import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "STAFF")
class BlogApiTest {

    private static final String ADMIN_URL = "/api/v1/admin/blog";

    @Autowired
    private MockMvc mvc;

    private void createPost(String title, String topic, boolean published) throws Exception {
        String json = """
                {"title":"%s","excerpt":"Short","body":"Long text","topic":"%s","readMinutes":4,"published":%s}"""
                .formatted(title, topic, published);
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void draftsStayHiddenFromThePublicBlog() throws Exception {
        createPost("Live Post", "Style Guide", true);
        createPost("Draft Post", "Style Guide", false);

        mvc.perform(get("/api/v1/blog")).andExpect(jsonPath("$.content", hasSize(1)));
        mvc.perform(get("/api/v1/blog/draft-post")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/blog/live-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedAt", notNullValue()));
    }

    @Test
    void filtersByTopic() throws Exception {
        createPost("Guide Post", "Style Guide", true);
        createPost("Tips Post", "Fashion Tips", true);

        mvc.perform(get("/api/v1/blog").param("topic", "Fashion Tips"))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Tips Post"));
    }

    @Test
    void duplicateTitleConflicts() throws Exception {
        createPost("Same Title", "Style Guide", false);

        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Same Title","excerpt":"S","body":"B","topic":"T","readMinutes":4,"published":false}"""))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotReachAdminBlog() throws Exception {
        mvc.perform(get(ADMIN_URL)).andExpect(status().isForbidden());
    }
}
