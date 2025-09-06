package com.zbob.modules.promotions;

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
class PromotionApiTest {

    private static final String URL = "/api/v1/admin/promotions";

    @Autowired
    private MockMvc mvc;

    private static String json(String code, String type, int value) {
        return "{\"code\":\"%s\",\"discountType\":\"%s\",\"discountValue\":%d,\"minimumCents\":0,\"active\":true}"
                .formatted(code, type, value);
    }

    @Test
    void codeIsStoredUppercase() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("winter10", "PERCENT", 10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WINTER10"));
    }

    @Test
    void duplicateCodeConflictsWhateverTheCase() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("SAME", "FIXED", 500)));

        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("same", "FIXED", 500)))
                .andExpect(status().isConflict());
    }

    @Test
    void percentAboveOneHundredIsRejected() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("BIG", "PERCENT", 101)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endDateMustFollowStartDate() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"DATES","discountType":"FIXED","discountValue":100,"minimumCents":0,"active":true,
                         "startsAt":"2026-06-02T00:00:00Z","endsAt":"2026-06-01T00:00:00Z"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotListPromotions() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
    }
}
