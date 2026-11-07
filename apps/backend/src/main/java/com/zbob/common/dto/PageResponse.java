package com.zbob.common.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** JSON envelope for paged lists; matches Page in @zbob/shared-types. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
