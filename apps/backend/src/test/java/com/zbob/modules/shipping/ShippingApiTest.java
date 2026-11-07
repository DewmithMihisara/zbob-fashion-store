package com.zbob.modules.shipping;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
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
class ShippingApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ShippingMethodRepository methods;

    @Test
    void publicListHidesInactiveMethodsAndSortsByPrice() throws Exception {
        methods.save(new ShippingMethod("Express", 1500, 2));
        methods.save(new ShippingMethod("Standard", 500, 5));
        ShippingMethod retired = new ShippingMethod("Old", 100, 9);
        retired.setActive(false);
        methods.save(retired);

        mvc.perform(get("/api/v1/shipping-methods"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Standard"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCreatesMethod() throws Exception {
        mvc.perform(post("/api/v1/admin/shipping-methods").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Standard\",\"priceCents\":500,\"estimatedDays\":5,\"active\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void adminListIsPagedAndIncludesInactive() throws Exception {
        methods.save(new ShippingMethod("Aaa", 100, 1));
        ShippingMethod off = new ShippingMethod("Bbb", 100, 1);
        off.setActive(false);
        methods.save(off);

        mvc.perform(get("/api/v1/admin/shipping-methods").param("size", "1").param("page", "1"))
                .andExpect(jsonPath("$.content[0].name").value("Bbb"))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void anonymousCannotCreateMethod() throws Exception {
        mvc.perform(post("/api/v1/admin/shipping-methods").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Standard\",\"priceCents\":500,\"estimatedDays\":5,\"active\":true}"))
                .andExpect(status().isUnauthorized());
    }
}
