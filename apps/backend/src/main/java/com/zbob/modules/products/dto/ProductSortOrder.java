package com.zbob.modules.products.dto;

import com.zbob.exception.BadRequestException;
import java.util.Arrays;
import org.springframework.data.domain.Sort;

/** The sort choices offered to shoppers. A fixed list, so visitors can not sort by internal fields. */
public enum ProductSortOrder {
    NEWEST("newest", Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))),
    PRICE_LOW("price-asc", Sort.by(Sort.Direction.ASC, "priceCents").and(Sort.by("id"))),
    PRICE_HIGH("price-desc", Sort.by(Sort.Direction.DESC, "priceCents").and(Sort.by("id"))),
    NAME("name", Sort.by(Sort.Direction.ASC, "name").and(Sort.by("id")));

    private final String value;
    private final Sort sort;

    ProductSortOrder(String value, Sort sort) {
        this.value = value;
        this.sort = sort;
    }

    public Sort sort() {
        return sort;
    }

    public static ProductSortOrder from(String value) {
        return Arrays.stream(values())
                .filter(order -> order.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown sort order: " + value));
    }
}
