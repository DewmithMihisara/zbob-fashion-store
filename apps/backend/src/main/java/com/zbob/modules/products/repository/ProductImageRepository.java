package com.zbob.modules.products.repository;

import com.zbob.modules.products.entity.ProductImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByPositionAscIdAsc(Long productId);

    List<ProductImage> findByProductIdInOrderByPositionAscIdAsc(Collection<Long> productIds);

    int countByProductId(Long productId);
}
