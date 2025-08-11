package com.zbob.modules.reviews.repository;

import com.zbob.modules.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByApprovedTrue(Pageable pageable);

    Page<Review> findByApproved(boolean approved, Pageable pageable);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.approved = true")
    double averageApprovedRating();

    long countByApprovedTrue();
}
