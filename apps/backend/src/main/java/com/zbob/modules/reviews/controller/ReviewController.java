package com.zbob.modules.reviews.controller;

import com.zbob.modules.reviews.dto.CustomerReviewRequest;
import com.zbob.modules.reviews.dto.ReviewResponse;
import com.zbob.modules.reviews.dto.ReviewSummaryResponse;
import com.zbob.modules.reviews.service.ReviewService;
import com.zbob.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping
    ReviewSummaryResponse summary() {
        return reviews.summary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    ReviewResponse submit(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CustomerReviewRequest request) {
        return reviews.submit(user.id(), request);
    }
}
