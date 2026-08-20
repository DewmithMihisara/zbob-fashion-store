package com.zbob.modules.categories.mapper;

import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.categories.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getBadge(), c.getPosition(),
                c.getSizeGuide());
    }
}
