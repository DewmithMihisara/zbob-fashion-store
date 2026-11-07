package com.zbob.modules.reviews.controller;

import com.zbob.modules.reviews.dto.ProductReviewsResponse;
import com.zbob.modules.reviews.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductReviewController {

    private final ReviewService reviews;

    public ProductReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/api/v1/products/{slug}/reviews")
    ProductReviewsResponse forProduct(@PathVariable String slug,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviews.forProduct(slug, pageable);
    }
}
