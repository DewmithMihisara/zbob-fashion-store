package com.zbob.modules.wishlist;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.products.entity.Product;
import com.zbob.modules.users.entity.User;
import com.zbob.support.TestData;
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
class WishlistApiTest {

    private static final String URL = "/api/v1/me/wishlist";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    private User customer;
    private Product coat;
    private Product tee;

    @BeforeEach
    void setUp() {
        customer = data.customer("wish@zbob.test");
        coat = data.variant("Wish Coat", 9000, 1).getProduct();
        tee = data.variant("Wish Tee", 2000, 1).getProduct();
    }

    private String auth() {
        return data.bearer(customer);
    }

    @Test
    void savesListsAndRemoves() throws Exception {
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth())).andExpect(status().isNoContent());
        mvc.perform(put(URL + "/" + tee.getId()).header("Authorization", auth())).andExpect(status().isNoContent());

        mvc.perform(get(URL).header("Authorization", auth())).andExpect(jsonPath("$.content", hasSize(2)));
        mvc.perform(get(URL + "/ids").header("Authorization", auth())).andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(delete(URL + "/" + coat.getId()).header("Authorization", auth())).andExpect(status().isNoContent());
        mvc.perform(get(URL).header("Authorization", auth()))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Wish Tee"));
    }

    @Test
    void savingTwiceKeepsOneEntry() throws Exception {
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth()));
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth())).andExpect(status().isNoContent());

        mvc.perform(get(URL + "/ids").header("Authorization", auth())).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void unpublishedProductsDropOutOfTheList() throws Exception {
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth()));
        coat.setPublished(false);

        mvc.perform(get(URL).header("Authorization", auth())).andExpect(jsonPath("$.content", hasSize(0)));
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth())).andExpect(status().isNotFound());
    }

    @Test
    void eachCustomerSeesOnlyTheirOwnList() throws Exception {
        mvc.perform(put(URL + "/" + coat.getId()).header("Authorization", auth()));
        User other = data.customer("other-wish@zbob.test");

        mvc.perform(get(URL).header("Authorization", data.bearer(other))).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}
