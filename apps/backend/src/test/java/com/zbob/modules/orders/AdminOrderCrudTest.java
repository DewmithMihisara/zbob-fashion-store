package com.zbob.modules.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
import com.zbob.modules.users.entity.User;
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
@WithMockUser(roles = "ADMIN")
class AdminOrderCrudTest {

    private static final String URL = "/api/v1/admin/orders";
    private static final String ADDRESS = """
            {"recipient":"Sam","line1":"1 Main St","city":"Town","postalCode":"1000","country":"LK"}""";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private ShippingMethodRepository shippingMethods;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private OrderRepository orders;

    @Autowired
    private EntityManager entityManager;

    private User customer;
    private ProductVariant tee;
    private ShippingMethod shipping;

    @BeforeEach
    void setUp() {
        customer = data.customer("phone@zbob.test");
        tee = data.variant("Phone Tee", 3000, 5);
        shipping = shippingMethods.save(new ShippingMethod("Standard", 500, 5));
    }

    private ResultActions create(int quantity) throws Exception {
        return mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                {"customerId":%d,"shippingMethodId":%d,"items":[{"variantId":%d,"quantity":%d}],"address":%s}"""
                .formatted(customer.getId(), shipping.getId(), tee.getId(), quantity, ADDRESS)));
    }

    private int createdId() throws Exception {
        return JsonPath.read(create(2).andReturn().getResponse().getContentAsString(), "$.id");
    }

    private int stock() {
        entityManager.flush();
        entityManager.clear();
        return variants.findById(tee.getId()).orElseThrow().getStockQuantity();
    }

    @Test
    void createsOrderWithProductPricesAndReservesStock() throws Exception {
        create(2)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotalCents").value(6000))
                .andExpect(jsonPath("$.totalCents").value(6500));

        assertThat(stock()).isEqualTo(3);
    }

    @Test
    void refusesMoreThanInStockAndKeepsStock() throws Exception {
        create(6).andExpect(status().isConflict());
    }

    @Test
    void requiresAtLeastOneItem() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"customerId":%d,"shippingMethodId":%d,"items":[],"address":%s}"""
                        .formatted(customer.getId(), shipping.getId(), ADDRESS)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void itemsArePaged() throws Exception {
        int id = createdId();

        mvc.perform(get(URL + "/" + id + "/items").param("size", "1"))
                .andExpect(jsonPath("$.content[0].quantity").value(2))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void addressChangesUntilTheOrderShips() throws Exception {
        int id = createdId();
        String updated = ADDRESS.replace("Sam", "Alex");

        mvc.perform(put(URL + "/" + id + "/address").contentType(MediaType.APPLICATION_JSON).content(updated))
                .andExpect(jsonPath("$.recipient").value("Alex"));

        mvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"));
        mvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"SHIPPED\"}"));
        mvc.perform(put(URL + "/" + id + "/address").contentType(MediaType.APPLICATION_JSON).content(ADDRESS))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyCancelledOrdersCanBeDeleted() throws Exception {
        int id = createdId();

        mvc.perform(delete(URL + "/" + id)).andExpect(status().isConflict());

        mvc.perform(patch(URL + "/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"));
        assertThat(stock()).isEqualTo(5);
        mvc.perform(delete(URL + "/" + id)).andExpect(status().isNoContent());
        assertThat(orders.findById((long) id)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCanNotDeleteOrders() throws Exception {
        mvc.perform(delete(URL + "/1")).andExpect(status().isForbidden());
    }
}
