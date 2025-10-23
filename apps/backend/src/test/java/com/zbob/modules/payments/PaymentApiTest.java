package com.zbob.modules.payments;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class PaymentApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Test
    void startingAPaymentReportsNotConfigured() throws Exception {
        String token = data.bearer(data.customer("pay@zbob.test"));

        mvc.perform(post("/api/v1/orders/1/payments").header("Authorization", token))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code").value("PAYMENTS_NOT_CONFIGURED"));
    }

    @Test
    void anonymousCannotStartAPayment() throws Exception {
        mvc.perform(post("/api/v1/orders/1/payments")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanSeeAnOrdersPayments() throws Exception {
        mvc.perform(get("/api/v1/admin/orders/1/payments")).andExpect(status().isOk());
    }
}
