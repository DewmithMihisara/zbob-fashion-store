package com.zbob.modules.reports;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderItem;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderItemRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.support.TestData;
import org.junit.jupiter.api.BeforeEach;
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
@WithMockUser(roles = "STAFF")
class SalesReportTest {

    private static final String URL = "/api/v1/admin/reports/sales";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private OrderItemRepository items;

    private User customer;

    @BeforeEach
    void setUp() {
        customer = data.customer("report@zbob.test");
    }

    private void order(OrderStatus status, int total, String product, int units) {
        Order order = new Order("ZB-REP-" + System.nanoTime(), customer.getId());
        order.setTotals(total, 0, 0, null, "Standard");
        order.setAddress("Sam", "1 Main St", null, "Town", null, "1000", "LK", null);
        order.setStatus(status);
        orders.saveAndFlush(order);
        items.save(new OrderItem(order.getId(), null, product, "SKU", "M", "Black", total / units, units));
    }

    @Test
    void countsRevenueOnlyFromPaidOrdersAndSkipsCancelledOnes() throws Exception {
        order(OrderStatus.PAID, 4000, "Report Coat", 1);
        order(OrderStatus.DELIVERED, 6000, "Report Tee", 3);
        order(OrderStatus.PENDING_PAYMENT, 1000, "Report Tee", 1);
        order(OrderStatus.CANCELLED, 9000, "Report Coat", 9);

        mvc.perform(get(URL).param("days", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").value(3))
                .andExpect(jsonPath("$.revenueCents").value(10000))
                .andExpect(jsonPath("$.averageOrderCents").value(5000))
                .andExpect(jsonPath("$.daily", hasSize(7)))
                .andExpect(jsonPath("$.daily[6].orders").value(3))
                .andExpect(jsonPath("$.bestSellers[0].productName").value("Report Tee"))
                .andExpect(jsonPath("$.bestSellers[0].units").value(4))
                .andExpect(jsonPath("$.bestSellers[1].units").value(1));
    }

    @Test
    void quietPeriodsAreZero() throws Exception {
        mvc.perform(get(URL).param("days", "3"))
                .andExpect(jsonPath("$.daily", hasSize(3)))
                .andExpect(jsonPath("$.daily[0].orders").value(0))
                .andExpect(jsonPath("$.averageOrderCents").value(0));
    }

    @Test
    void reportsStockHealth() throws Exception {
        data.variant("Report Empty", 1000, 0);
        data.variant("Report Low", 1000, 2);
        data.variant("Report Full", 1000, 50);

        mvc.perform(get(URL))
                .andExpect(jsonPath("$.stock.out").value(1))
                .andExpect(jsonPath("$.stock.low").value(1))
                .andExpect(jsonPath("$.stock.inStock").value(1));
    }

    @Test
    void refusesSillyRanges() throws Exception {
        mvc.perform(get(URL).param("days", "0")).andExpect(status().isBadRequest());
        mvc.perform(get(URL).param("days", "1000")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customersCanNotSeeSales() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isForbidden());
    }
}
