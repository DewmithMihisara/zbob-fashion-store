package com.zbob.modules.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.promotions.entity.DiscountType;
import com.zbob.modules.promotions.entity.Promotion;
import com.zbob.modules.promotions.repository.PromotionRepository;
import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestData.class)
class CheckoutQuoteTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private CustomerAddressRepository addresses;

    @Autowired
    private ShippingMethodRepository shipping;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private ProductVariantRepository variants;

    @Autowired
    private OrderRepository orders;

    private User customer;
    private ProductVariant tee;
    private CustomerAddress address;
    private ShippingMethod standard;

    @BeforeEach
    void setUp() {
        customer = data.customer("checkout@zbob.test");
        tee = data.variant("Checkout Tee", 2500, 3);
        address = new CustomerAddress(customer.getId());
        address.setRecipient("Sam");
        address.setLine1("1 Main St");
        address.setCity("Town");
        address.setPostalCode("1000");
        address.setCountry("LK");
        addresses.save(address);
        standard = shipping.save(new ShippingMethod("Standard", 400, 3));
        promotions.save(new Promotion("TEN", DiscountType.PERCENT, 10));
    }

    private ResultActions quote(String json) throws Exception {
        return mvc.perform(post("/api/v1/checkout/quote").header("Authorization", data.bearer(customer))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void ordersNowPricesJustThatItemWithDeliveryAndDiscount() throws Exception {
        quote("""
                {"addressId":%d,"shippingMethodId":%d,"promotionCode":"ten","buyNow":{"variantId":%d,"quantity":2}}"""
                .formatted(address.getId(), standard.getId(), tee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines", hasSize(1)))
                .andExpect(jsonPath("$.subtotalCents").value(5000))
                .andExpect(jsonPath("$.discountCents").value(500))
                .andExpect(jsonPath("$.shippingCents").value(400))
                .andExpect(jsonPath("$.totalCents").value(4900))
                .andExpect(jsonPath("$.promotionCode").value("TEN"))
                .andExpect(jsonPath("$.problems", hasSize(0)));
    }

    @Test
    void neverPlacesAnOrderOrReservesStock() throws Exception {
        long before = orders.count();
        quote("{\"buyNow\":{\"variantId\":%d,\"quantity\":3}}".formatted(tee.getId()));

        assertThat(orders.count()).isEqualTo(before);
        assertThat(variants.findById(tee.getId()).orElseThrow().getStockQuantity()).isEqualTo(3);
    }

    @Test
    void cannotBePaidWithoutAGateway() throws Exception {
        quote("""
                {"addressId":%d,"shippingMethodId":%d,"buyNow":{"variantId":%d,"quantity":1}}"""
                .formatted(address.getId(), standard.getId(), tee.getId()))
                .andExpect(jsonPath("$.paymentAvailable").value(false))
                .andExpect(jsonPath("$.readyToPay").value(false));
    }

    @Test
    void listsWhatIsMissing() throws Exception {
        quote("{\"buyNow\":{\"variantId\":%d,\"quantity\":9}}".formatted(tee.getId()))
                .andExpect(jsonPath("$.problems", hasItem("Choose a delivery address.")))
                .andExpect(jsonPath("$.problems", hasItem("Choose a delivery option.")))
                .andExpect(jsonPath("$.problems", hasItem("Some items do not have enough stock. Lower the quantity or remove them.")));
    }

    @Test
    void aBadPromotionCodeIsExplainedWithoutFailing() throws Exception {
        quote("{\"promotionCode\":\"NOPE\",\"buyNow\":{\"variantId\":%d,\"quantity\":1}}".formatted(tee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountCents").value(0))
                .andExpect(jsonPath("$.promotionError").value("This promotion code can not be used on this order"));
    }

    @Test
    void anEmptyCartHasNothingToCheckOut() throws Exception {
        quote("{}").andExpect(jsonPath("$.problems", hasItem("There is nothing to check out.")));
    }

    @Test
    void anotherCustomersAddressIsRefused() throws Exception {
        User other = data.customer("other-checkout@zbob.test");
        CustomerAddress theirs = new CustomerAddress(other.getId());
        theirs.setRecipient("X");
        theirs.setLine1("2 Side St");
        theirs.setCity("Town");
        theirs.setPostalCode("1000");
        theirs.setCountry("LK");
        addresses.save(theirs);

        quote("{\"addressId\":%d}".formatted(theirs.getId())).andExpect(status().isBadRequest());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(post("/api/v1/checkout/quote").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
