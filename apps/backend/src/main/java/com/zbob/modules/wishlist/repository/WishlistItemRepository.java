package com.zbob.modules.wishlist.repository;

import com.zbob.modules.wishlist.entity.WishlistItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /** Hides products that were unpublished after being saved. */
    Page<WishlistItem> findByUserIdAndProductPublishedTrue(Long userId, Pageable pageable);

    @Query("select w.product.id from WishlistItem w where w.userId = :userId and w.product.published = true")
    List<Long> findPublishedProductIds(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("delete from WishlistItem w where w.userId = :userId and w.product.id = :productId")
    int deleteByUserIdAndProductId(Long userId, Long productId);
}
