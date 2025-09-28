package com.zbob.modules.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.promotions.entity.DiscountType;
import com.zbob.modules.promotions.entity.Promotion;
import com.zbob.modules.promotions.repository.PromotionRepository;
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
class OrderApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private CustomerAddressRepository addresses;

    @Autowired
    private ShippingMethodRepository shippingMethods;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private EntityManager entityManager;

    private User customer;
    private ProductVariant tee;
    private CustomerAddress address;
    private ShippingMethod shipping;

    @BeforeEach
    void setUp() {
        customer = data.customer("order@zbob.test");
        tee = data.variant("Order Tee", 3000, 5);
        address = new CustomerAddress(customer.getId());
        address.setRecipient("Sam");
        address.setLine1("1 Main St");
        address.setCity("Town");
        address.setPostalCode("1000");
        address.setCountry("LK");
        addresses.save(address);
        shipping = shippingMethods.save(new ShippingMethod("Standard", 500, 5));
    }

    private void addToCart(ProductVariant variant, int quantity) throws Exception {
        mvc.perform(post("/api/v1/cart/items").header("Authorization", data.bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":%d,\"quantity\":%d}".formatted(variant.getId(), quantity)))
                .andExpect(status().isOk());
    }

    private ResultActions place(String promotionCode) throws Exception {
        String code = promotionCode == null ? "null" : "\"" + promotionCode + "\"";
        return mvc.perform(post("/api/v1/orders").header("Authorization", data.bearer(customer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"addressId\":%d,\"shippingMethodId\":%d,\"promotionCode\":%s}"
                        .formatted(address.getId(), shipping.getId(), code)));
    }

    private int stockOf(ProductVariant variant) {
        entityManager.flush();
        entityManager.clear();
        return variants.findById(variant.getId()).orElseThrow().getStockQuantity();
    }

    @Test
    void placesOrderWithTotalsAndEmptiesTheCart() throws Exception {
        addToCart(tee, 2);

        place(null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.subtotalCents").value(6000))
                .andExpect(jsonPath("$.shippingCents").value(500))
                .andExpect(jsonPath("$.totalCents").value(6500))
                .andExpect(jsonPath("$.items", hasSize(1)));

        mvc.perform(get("/api/v1/cart").header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$.items", hasSize(0)));
        assertThat(stockOf(tee)).isEqualTo(3);
    }

    @Test
    void appliesPromotionCode() throws Exception {
        promotions.save(new Promotion("TEN", DiscountType.PERCENT, 10));
        addToCart(tee, 1);

        place("ten")
                .andExpect(jsonPath("$.discountCents").value(300))
                .andExpect(jsonPath("$.totalCents").value(3200))
                .andExpect(jsonPath("$.promotionCode").value("TEN"));
    }

    @Test
    void unusablePromotionCodeFailsTheOrder() throws Exception {
        addToCart(tee, 1);

        place("NOPE").andExpect(status().isBadRequest());
        assertThat(stockOf(tee)).isEqualTo(5);
    }

    @Test
    void emptyCartCannotBeOrdered() throws Exception {
        place(null).andExpect(status().isBadRequest());
    }

    @Test
    void outOfStockFailsTheOrder() throws Exception {
        addToCart(tee, 5);
        variants.findById(tee.getId()).orElseThrow().setStockQuantity(1);
        variants.flush();

        place(null).andExpect(status().isConflict());
    }

    @Test
    void cannotUseAnotherCustomersAddress() throws Exception {
        CustomerAddress other = new CustomerAddress(data.customer("x@zbob.test").getId());
        other.setRecipient("X");
        other.setLine1("2 Side St");
        other.setCity("Town");
        other.setPostalCode("1000");
        other.setCountry("LK");
        addresses.save(other);
        addToCart(tee, 1);

        mvc.perform(post("/api/v1/orders").header("Authorization", data.bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":%d,\"shippingMethodId\":%d}".formatted(other.getId(), shipping.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerSeesOnlyTheirOwnOrders() throws Exception {
        addToCart(tee, 1);
        int orderId = JsonPath.read(place(null).andReturn().getResponse().getContentAsString(), "$.id");
        User other = data.customer("y@zbob.test");

        mvc.perform(get("/api/v1/orders/" + orderId).header("Authorization", data.bearer(customer)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/orders/" + orderId).header("Authorization", data.bearer(other)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void cancellingReturnsStockAndFinishedOrdersCanNotChange() throws Exception {
        addToCart(tee, 2);
        int orderId = JsonPath.read(place(null).andReturn().getResponse().getContentAsString(), "$.id");
        String url = "/api/v1/admin/orders/" + orderId + "/status";

        mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
        assertThat(stockOf(tee)).isEqualTo(5);

        mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAID\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void anonymousCannotReachAdminOrders() throws Exception {
        mvc.perform(get("/api/v1/admin/orders")).andExpect(status().isUnauthorized());
    }
}
