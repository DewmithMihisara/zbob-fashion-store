package com.zbob.modules.reviews;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zbob.modules.reviews.entity.Review;
import com.zbob.modules.reviews.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewApiTest {

    private static final String ADMIN_URL = "/api/v1/admin/reviews";
    private static final String BODY =
            "{\"authorName\":\"Sam\",\"headline\":\"Great\",\"body\":\"Fits well\",\"rating\":5,\"approved\":true}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReviewRepository reviews;

    private void saveReview(int rating, boolean approved) {
        Review review = new Review("Sam", "Headline", "Body", rating);
        review.setApproved(approved);
        reviews.save(review);
    }

    @Test
    void summaryCountsOnlyApprovedReviews() throws Exception {
        saveReview(5, true);
        saveReview(4, true);
        saveReview(1, false);

        mvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviews", hasSize(2)));
    }

    @Test
    void noReviewsGivesZeroRatherThanAnInventedScore() throws Exception {
        mvc.perform(get("/api/v1/reviews"))
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCreatesAndFiltersByApproval() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        saveReview(3, false);

        mvc.perform(get(ADMIN_URL).param("approved", "false")).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void ratingOutsideOneToFiveIsRejected() throws Exception {
        mvc.perform(post(ADMIN_URL).contentType(MediaType.APPLICATION_JSON).content(BODY.replace("\"rating\":5", "\"rating\":6")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousCannotSubmitReview() throws Exception {
        mvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"headline\":\"H\",\"body\":\"B\",\"rating\":5}"))
                .andExpect(status().isUnauthorized());
    }
}
