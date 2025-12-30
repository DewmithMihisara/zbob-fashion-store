package com.zbob.modules.reviews;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.reviews.entity.Review;
import com.zbob.modules.reviews.repository.ReviewRepository;
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
class ProductReviewsTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData data;

    @Autowired
    private ReviewRepository reviews;

    private ProductVariant tee;
    private User customer;

    @BeforeEach
    void setUp() {
        tee = data.variant("Review Tee", 1000, 5);
        customer = data.customer("reviewer@zbob.test");
    }

    private ResultActions submit(User user, int rating) throws Exception {
        return mvc.perform(post("/api/v1/reviews").header("Authorization", data.bearer(user)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":%d,\"headline\":\"Nice\",\"body\":\"Fits well\",\"rating\":%d}".formatted(tee.getProduct().getId(), rating)));
    }

    private void approved(int rating, Long productId) {
        Review review = new Review("Someone", "Headline", "Body", rating);
        review.setApproved(true);
        review.setProductId(productId);
        reviews.save(review);
    }

    @Test
    void customerReviewWaitsForApproval() throws Exception {
        submit(customer, 5).andExpect(status().isCreated()).andExpect(jsonPath("$.approved").value(false));

        mvc.perform(get("/api/v1/products/review-tee/reviews"))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.reviews.content", hasSize(0)));
    }

    @Test
    void oneReviewPerCustomerPerProduct() throws Exception {
        submit(customer, 5).andExpect(status().isCreated());

        submit(customer, 1).andExpect(status().isConflict());
    }

    @Test
    void productScoreUsesOnlyThatProductsApprovedReviews() throws Exception {
        approved(5, tee.getProduct().getId());
        approved(4, tee.getProduct().getId());
        approved(1, null);

        mvc.perform(get("/api/v1/products/review-tee/reviews"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.reviews.content[0].authorName").value("Someone"))
                .andExpect(jsonPath("$.reviews.content[0].approved").doesNotExist());
    }

    @Test
    void draftProductsHaveNoPublicReviews() throws Exception {
        tee.getProduct().setPublished(false);

        mvc.perform(get("/api/v1/products/review-tee/reviews")).andExpect(status().isNotFound());
        submit(customer, 5).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void adminListShowsTheProductName() throws Exception {
        approved(5, tee.getProduct().getId());

        mvc.perform(get("/api/v1/admin/reviews"))
                .andExpect(jsonPath("$.content[0].productName").value("Review Tee"));
    }
}
