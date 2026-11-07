package com.zbob.modules.checkout;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.support.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Deliberately not transactional. Inside a test transaction nothing commits, which hid a bug where an
 * unusable promo code made the real request fail with a 500 at commit time.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestData.class)
class CheckoutCommitTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private UserRepository users;

    @Autowired
    private ProductRepository products;

    private User customer;
    private ProductVariant tee;

    @BeforeEach
    void setUp() {
        long n = System.nanoTime();
        customer = data.customer("commit-" + n + "@zbob.test");
        tee = data.variant("Commit Tee " + n, 1000, 2);
    }

    @AfterEach
    void tearDown() {
        users.delete(customer);
        products.deleteById(tee.getProduct().getId());
    }

    @Test
    void anUnusableCodeIsExplainedInAnOkAnswer() throws Exception {
        mvc.perform(post("/api/v1/checkout/quote").header("Authorization", data.bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promotionCode\":\"NOT-REAL\",\"buyNow\":{\"variantId\":%d,\"quantity\":1}}".formatted(tee.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionError").value("This promotion code can not be used on this order"))
                .andExpect(jsonPath("$.totalCents").value(1000));
    }
}
