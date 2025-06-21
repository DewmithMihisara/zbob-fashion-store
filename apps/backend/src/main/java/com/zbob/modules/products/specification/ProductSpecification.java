package com.zbob.modules.products.specification;

import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductBadge;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> published() {
        return (root, query, cb) -> cb.isTrue(root.get("published"));
    }

    public static Specification<Product> inCategory(String categorySlug) {
        return (root, query, cb) -> StringUtils.hasText(categorySlug)
                ? cb.equal(root.join("category").get("slug"), categorySlug)
                : cb.conjunction();
    }

    public static Specification<Product> hasBadge(ProductBadge badge) {
        return (root, query, cb) -> badge != null ? cb.equal(root.get("badge"), badge) : cb.conjunction();
    }

    /** Case-insensitive name match; LIKE wildcards typed by the user are treated as plain text. */
    public static Specification<Product> nameContains(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String escaped = search.trim().toLowerCase()
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            return cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '\\');
        };
    }
}
