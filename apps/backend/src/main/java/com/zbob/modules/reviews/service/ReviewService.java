package com.zbob.modules.reviews.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.reviews.dto.CustomerReviewRequest;
import com.zbob.modules.reviews.dto.ReviewRequest;
import com.zbob.modules.reviews.dto.ReviewResponse;
import com.zbob.modules.reviews.dto.ReviewSummaryResponse;
import com.zbob.modules.reviews.entity.Review;
import com.zbob.modules.reviews.repository.ReviewRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final UserRepository users;
    private final ProductRepository products;

    public ReviewService(ReviewRepository reviews, UserRepository users, ProductRepository products) {
        this.reviews = reviews;
        this.users = users;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse summary() {
        var latest = reviews.findByApprovedTrue(PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new ReviewSummaryResponse(
                Math.round(reviews.averageApprovedRating() * 10) / 10.0,
                reviews.countByApprovedTrue(),
                latest.getContent().stream().map(ReviewService::toResponse).toList());
    }

    /** Customer reviews wait for approval and take the author's name from their account. */
    @Transactional
    public ReviewResponse submit(Long userId, CustomerReviewRequest request) {
        User user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!products.existsById(request.productId())) {
            throw new BadRequestException("Unknown product: " + request.productId());
        }
        Review review = new Review(user.getFullName(), request.headline(), request.body(), request.rating());
        review.setUserId(userId);
        review.setProductId(request.productId());
        return toResponse(reviews.save(review));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> listAdmin(Boolean approved, Pageable pageable) {
        var page = approved == null ? reviews.findAll(pageable) : reviews.findByApproved(approved, pageable);
        return PageResponse.of(page, ReviewService::toResponse);
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        Review review = new Review(request.authorName(), request.headline(), request.body(), request.rating());
        apply(review, request);
        return toResponse(reviews.save(review));
    }

    @Transactional
    public ReviewResponse update(Long id, ReviewRequest request) {
        Review review = find(id);
        apply(review, request);
        return toResponse(review);
    }

    @Transactional
    public void delete(Long id) {
        reviews.delete(find(id));
    }

    private Review find(Long id) {
        return reviews.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    private static void apply(Review review, ReviewRequest request) {
        review.setAuthorName(request.authorName());
        review.setAuthorTitle(request.authorTitle());
        review.setHeadline(request.headline());
        review.setBody(request.body());
        review.setRating(request.rating());
        review.setApproved(request.approved());
    }

    private static ReviewResponse toResponse(Review r) {
        return new ReviewResponse(r.getId(), r.getAuthorName(), r.getAuthorTitle(), r.getHeadline(), r.getBody(),
                r.getRating(), r.isApproved());
    }
}
