package com.zbob.modules.returns;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.support.TestData;
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
class ReturnApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private OrderRepository orders;

    private User customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = data.customer("returns@zbob.test");
        order = new Order("ZB-RET-" + System.nanoTime(), customer.getId());
        order.setTotals(5000, 0, 0, null, "Standard");
        order.setAddress("Sam", "1 Main St", null, "Town", null, "1000", "LK", null);
        order.setStatus(OrderStatus.DELIVERED);
        orders.saveAndFlush(order);
    }

    private ResultActions requestReturn(User user, String reason) throws Exception {
        return mvc.perform(post("/api/v1/orders/" + order.getId() + "/returns").header("Authorization", data.bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"%s\"}".formatted(reason)));
    }

    @Test
    void customerRequestsAReturnForADeliveredOrderOnce() throws Exception {
        requestReturn(customer, "Too small").andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED"));

        requestReturn(customer, "Again").andExpect(status().isConflict());
        mvc.perform(get("/api/v1/orders/" + order.getId() + "/returns").header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].reason").value("Too small"));
    }

    @Test
    void anUndeliveredOrderCanNotBeReturned() throws Exception {
        order.setStatus(OrderStatus.SHIPPED);
        orders.flush();

        requestReturn(customer, "Too small").andExpect(status().isConflict());
    }

    @Test
    void anotherCustomerCanNotRequestOne() throws Exception {
        requestReturn(data.customer("someone@zbob.test"), "Mine now").andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffApproveThenCompleteAndFinishedReturnsStayFinished() throws Exception {
        String body = requestReturn(customer, "Wrong colour").andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(body, "$.id");

        mvc.perform(get("/api/v1/admin/returns").param("status", "REQUESTED"))
                .andExpect(jsonPath("$.content[0].orderNumber").value(order.getOrderNumber()))
                .andExpect(jsonPath("$.content[0].customerEmail").value("returns@zbob.test"));
        mvc.perform(patch("/api/v1/admin/returns/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"staffNote\":\"Send it back within 14 days\"}"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.staffNote").value("Send it back within 14 days"));
        mvc.perform(patch("/api/v1/admin/returns/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(patch("/api/v1/admin/returns/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"REJECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCanNotDecideReturns() throws Exception {
        mvc.perform(get("/api/v1/admin/returns")).andExpect(status().isForbidden());
    }
}
