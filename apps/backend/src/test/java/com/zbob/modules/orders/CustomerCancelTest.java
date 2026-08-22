package com.zbob.modules.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderItem;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderItemRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.support.TestData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
class CustomerCancelTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private OrderItemRepository items;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private EntityManager entityManager;

    private User customer;
    private ProductVariant tee;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = data.customer("cancel@zbob.test");
        tee = data.variant("Cancel Tee", 1000, 1);
        order = new Order("ZB-CANCEL-" + System.nanoTime(), customer.getId());
        order.setTotals(2000, 0, 0, null, "Standard");
        order.setAddress("Sam", "1 Main St", null, "Town", null, "1000", "LK", null);
        orders.save(order);
        items.save(new OrderItem(order.getId(), tee.getId(), "Cancel Tee", tee.getSku(), "M", "Black", 1000, 2));
    }

    @Test
    void anUnpaidOrderCanBeCancelledAndItsStockReturns() throws Exception {
        mvc.perform(post("/api/v1/orders/" + order.getId() + "/cancel").header("Authorization", data.bearer(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        entityManager.flush();
        entityManager.clear();
        assertThat(variants.findById(tee.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
    }

    @Test
    void aPaidOrderMustBeCancelledByTheShop() throws Exception {
        order.setStatus(OrderStatus.PAID);

        mvc.perform(post("/api/v1/orders/" + order.getId() + "/cancel").header("Authorization", data.bearer(customer)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This order can no longer be cancelled online. Please contact us."));
    }

    @Test
    void anotherCustomerCanNotCancelIt() throws Exception {
        User other = data.customer("not-mine@zbob.test");

        mvc.perform(post("/api/v1/orders/" + order.getId() + "/cancel").header("Authorization", data.bearer(other)))
                .andExpect(status().isNotFound());
    }
}
