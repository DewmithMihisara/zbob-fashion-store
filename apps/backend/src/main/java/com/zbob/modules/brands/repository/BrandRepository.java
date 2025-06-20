package com.zbob.modules.brands.repository;

import com.zbob.modules.brands.entity.Brand;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderByNameAsc();

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}
