package com.zbob.modules.customers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import com.zbob.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
class AdminCustomerCrudTest {

    private static final String URL = "/api/v1/admin/customers";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private UserRepository users;

    @Autowired
    private OrderRepository orders;

    @Test
    @WithMockUser(roles = "STAFF")
    void createsCustomerThatCanNotBeStaff() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"New@zbob.test\",\"password\":\"password123\",\"fullName\":\"New\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@zbob.test"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void editsCustomerAndRefusesATakenEmail() throws Exception {
        User first = data.customer("first@zbob.test");
        data.customer("taken@zbob.test");

        mvc.perform(put(URL + "/" + first.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"changed@zbob.test\",\"fullName\":\"Changed\",\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Changed"))
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(put(URL + "/" + first.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"TAKEN@zbob.test\",\"fullName\":\"Changed\",\"active\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeletesCustomerWithoutOrders() throws Exception {
        User customer = data.customer("gone@zbob.test");

        mvc.perform(delete(URL + "/" + customer.getId())).andExpect(status().isNoContent());

        assertThat(users.findById(customer.getId())).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void customerWithOrdersCanNotBeDeleted() throws Exception {
        User customer = data.customer("buyer@zbob.test");
        Order order = new Order("ZB-TEST-1", customer.getId());
        order.setTotals(100, 0, 0, null, "Standard");
        order.setAddress("Sam", "1 Main St", null, "Town", null, "1000", "LK", null);
        orders.save(order);

        mvc.perform(delete(URL + "/" + customer.getId())).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanNotDeleteCustomers() throws Exception {
        User customer = data.customer("safe@zbob.test");

        mvc.perform(delete(URL + "/" + customer.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void staffAccountsCanNotBeChangedHere() throws Exception {
        Long adminId = users.findByEmailIgnoreCase("admin@zbob.test").orElseThrow().getId();

        mvc.perform(delete(URL + "/" + adminId)).andExpect(status().isNotFound());
    }
}
