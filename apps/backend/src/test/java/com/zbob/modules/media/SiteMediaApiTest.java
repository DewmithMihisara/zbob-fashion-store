package com.zbob.modules.media;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SiteMediaApiTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final String ADMIN_URL = "/api/v1/admin/site-media/";

    @Autowired
    private MockMvc mvc;

    private static RequestBuilder upload(String slot) {
        return multipart(HttpMethod.PUT, ADMIN_URL + slot).file(new MockMultipartFile("file", "a.png", "image/png", PNG));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void emptySlotListsWithNullUrl() throws Exception {
        mvc.perform(get("/api/v1/admin/site-media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slot").value("home-hero-1"))
                .andExpect(jsonPath("$[0].url").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void uploadShowsOnPublicMap() throws Exception {
        mvc.perform(upload("home-story")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/site-media"))
                .andExpect(jsonPath("$.home-story", startsWith("http://localhost:8080/uploads/site/home-story/")));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void replacingKeepsOneImagePerSlot() throws Exception {
        mvc.perform(upload("shop-hero")).andExpect(status().isOk());
        mvc.perform(upload("shop-hero")).andExpect(status().isOk());

        mvc.perform(get("/api/v1/admin/site-media"))
                .andExpect(jsonPath("$[?(@.slot == 'shop-hero')].url").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void clearReturnsSlotToPlaceholder() throws Exception {
        mvc.perform(upload("blog-hero")).andExpect(status().isOk());

        mvc.perform(delete(ADMIN_URL + "blog-hero")).andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/site-media")).andExpect(jsonPath("$.blog-hero").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void unknownSlotIsNotFound() throws Exception {
        mvc.perform(upload("no-such-slot")).andExpect(status().isNotFound());
    }

    @Test
    void anonymousCanReadButNotUpload() throws Exception {
        mvc.perform(get("/api/v1/site-media")).andExpect(status().isOk());
        mvc.perform(upload("home-story")).andExpect(status().isUnauthorized());
    }
}
