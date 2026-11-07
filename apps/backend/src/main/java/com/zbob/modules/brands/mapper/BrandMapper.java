package com.zbob.modules.brands.mapper;

import com.zbob.modules.brands.dto.response.BrandResponse;
import com.zbob.modules.brands.entity.Brand;

public final class BrandMapper {

    private BrandMapper() {
    }

    public static BrandResponse toResponse(Brand b) {
        return new BrandResponse(b.getId(), b.getName(), b.getSlug());
    }
}
