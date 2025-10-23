package com.zbob.modules.inventory.repository;

import com.zbob.modules.inventory.entity.ProductVariant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductIdOrderBySizeAscColorAsc(Long productId);

    /** Returns 0 when there is not enough stock, so two simultaneous orders can never oversell. */
    @Modifying
    @Query("update ProductVariant v set v.stockQuantity = v.stockQuantity - :quantity where v.id = :id and v.stockQuantity >= :quantity")
    int reserveStock(Long id, int quantity);

    @Modifying
    @Query("update ProductVariant v set v.stockQuantity = v.stockQuantity + :quantity where v.id = :id")
    int restoreStock(Long id, int quantity);

    @Query("select count(v) from ProductVariant v where v.stockQuantity <= v.lowStockThreshold")
    long countLowStock();

    long countByStockQuantity(int stockQuantity);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    boolean existsByProductIdAndSizeAndColor(Long productId, String size, String color);

    boolean existsByProductIdAndSizeAndColorAndIdNot(Long productId, String size, String color, Long id);

    @Query("select distinct v.product.id from ProductVariant v where v.product.id in :productIds and v.stockQuantity > 0")
    List<Long> findProductIdsInStock(Collection<Long> productIds);

    @Query(value = "select v from ProductVariant v join fetch v.product where v.stockQuantity <= v.lowStockThreshold",
            countQuery = "select count(v) from ProductVariant v where v.stockQuantity <= v.lowStockThreshold")
    Page<ProductVariant> findLowStock(Pageable pageable);

    @Query(value = "select v from ProductVariant v join fetch v.product",
            countQuery = "select count(v) from ProductVariant v")
    Page<ProductVariant> findAllPaged(Pageable pageable);
}
