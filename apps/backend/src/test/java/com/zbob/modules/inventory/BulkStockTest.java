package com.zbob.modules.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.support.TestData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
@WithMockUser(roles = "STAFF")
class BulkStockTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private EntityManager entityManager;

    private ProductVariant a;
    private ProductVariant b;

    @BeforeEach
    void setUp() {
        a = data.variant("Bulk A", 1000, 5);
        b = data.variant("Bulk B", 1000, 1);
    }

    private ResultActions bulk(String mode, int amount) throws Exception {
        return mvc.perform(post("/api/v1/admin/inventory/bulk").contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantIds\":[%d,%d],\"mode\":\"%s\",\"amount\":%d}".formatted(a.getId(), b.getId(), mode, amount)));
    }

    private int stock(ProductVariant variant) {
        entityManager.flush();
        entityManager.clear();
        return variants.findById(variant.getId()).orElseThrow().getStockQuantity();
    }

    @Test
    void setsEveryChosenRow() throws Exception {
        bulk("SET", 12).andExpect(status().isOk()).andExpect(jsonPath("$.updated").value(2));

        assertThat(stock(a)).isEqualTo(12);
        assertThat(stock(b)).isEqualTo(12);
    }

    @Test
    void addsToEveryChosenRow() throws Exception {
        bulk("ADJUST", 3);

        assertThat(stock(a)).isEqualTo(8);
        assertThat(stock(b)).isEqualTo(4);
    }

    @Test
    void refusesAnyRowGoingBelowZeroAndChangesNothing() throws Exception {
        bulk("ADJUST", -2)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This would take stock below 0 for: bulk-b-m"));

        assertThat(stock(a)).isEqualTo(5);
    }

    @Test
    void refusesSettingBelowZero() throws Exception {
        bulk("SET", -1).andExpect(status().isBadRequest());
    }

    @Test
    void refusesMissingRows() throws Exception {
        mvc.perform(post("/api/v1/admin/inventory/bulk").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantIds\":[999999],\"mode\":\"SET\",\"amount\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCanNotChangeStock() throws Exception {
        bulk("SET", 100).andExpect(status().isForbidden());
    }
}
