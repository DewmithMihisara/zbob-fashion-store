package com.zbob.modules.carts;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.entity.ProductImage;
import com.zbob.modules.products.repository.ProductImageRepository;
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
class CartApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private ProductImageRepository images;

    @Autowired
    private ProductVariantRepository variants;

    private User customer;
    private ProductVariant tee;

    @BeforeEach
    void setUp() {
        customer = data.customer("cart@zbob.test");
        tee = data.variant("Cart Tee", 3500, 5);
    }

    private ResultActions add(User user, ProductVariant variant, int quantity) throws Exception {
        return mvc.perform(post("/api/v1/cart/items").header("Authorization", data.bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":%d,\"quantity\":%d}".formatted(variant.getId(), quantity)));
    }

    @Test
    void emptyCartHasNoItems() throws Exception {
        mvc.perform(get("/api/v1/cart").header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.subtotalCents").value(0));
    }

    @Test
    void addingTheSameItemTwiceCombinesQuantityAndPricesFromTheProduct() throws Exception {
        add(customer, tee, 1).andExpect(status().isOk());

        add(customer, tee, 2)
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.subtotalCents").value(10500));
    }

    @Test
    void lineShowsTheFirstPhotoOrNone() throws Exception {
        add(customer, tee, 1).andExpect(jsonPath("$.items[0].imageUrl").doesNotExist());

        images.save(new ProductImage(tee.getProduct(), "b.png", "http://x/b.png", null, 1));
        images.save(new ProductImage(tee.getProduct(), "a.png", "http://x/a.png", null, 0));

        mvc.perform(get("/api/v1/cart").header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$.items[0].imageUrl").value("http://x/a.png"));
    }

    @Test
    void lineIsFlaggedWhenStockDropsBelowItsQuantity() throws Exception {
        add(customer, tee, 3).andExpect(jsonPath("$.items[0].available").value(true));

        tee.setStockQuantity(1);
        variants.flush();

        mvc.perform(get("/api/v1/cart").header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$.items[0].available").value(false));
    }

    @Test
    void draftProductsCanNotBeAdded() throws Exception {
        tee.getProduct().setPublished(false);
        variants.flush();

        add(customer, tee, 1).andExpect(status().isBadRequest());
    }

    @Test
    void cannotAddMoreThanInStock() throws Exception {
        add(customer, tee, 6).andExpect(status().isConflict());
    }

    @Test
    void updatesAndRemovesItems() throws Exception {
        String body = add(customer, tee, 1).andReturn().getResponse().getContentAsString();
        int itemId = JsonPath.read(body, "$.items[0].id");

        mvc.perform(patch("/api/v1/cart/items/" + itemId).header("Authorization", data.bearer(customer))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":4}"))
                .andExpect(jsonPath("$.items[0].quantity").value(4));
        mvc.perform(delete("/api/v1/cart/items/" + itemId).header("Authorization", data.bearer(customer)))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void cannotChangeAnotherCustomersCartItem() throws Exception {
        String body = add(customer, tee, 1).andReturn().getResponse().getContentAsString();
        int itemId = JsonPath.read(body, "$.items[0].id");
        User other = data.customer("other@zbob.test");
        add(other, tee, 1);

        mvc.perform(delete("/api/v1/cart/items/" + itemId).header("Authorization", data.bearer(other)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get("/api/v1/cart")).andExpect(status().isUnauthorized());
    }
}
