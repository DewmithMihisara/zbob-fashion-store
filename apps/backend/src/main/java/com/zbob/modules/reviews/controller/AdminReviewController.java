package com.zbob.modules.reviews.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.reviews.dto.ReviewRequest;
import com.zbob.modules.reviews.dto.ReviewResponse;
import com.zbob.modules.reviews.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviews;

    public AdminReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping
    PageResponse<ReviewResponse> list(
            @RequestParam(required = false) Boolean approved,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviews.listAdmin(approved, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ReviewResponse create(@Valid @RequestBody ReviewRequest request) {
        return reviews.create(request);
    }

    @PutMapping("/{id}")
    ReviewResponse update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return reviews.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        reviews.delete(id);
    }
}
