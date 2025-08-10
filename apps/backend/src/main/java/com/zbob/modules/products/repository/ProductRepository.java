package com.zbob.modules.products.repository;

import com.zbob.modules.products.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlugAndPublishedTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByBrandId(Long brandId);

    @Query("""
            select p.category.id as categoryId, min(p.priceCents) as minCents, max(p.priceCents) as maxCents
            from Product p where p.published = true group by p.category.id""")
    List<CategoryPriceRange> findPublishedPriceRanges();

    interface CategoryPriceRange {
        Long getCategoryId();

        Integer getMinCents();

        Integer getMaxCents();
    }
}
