package com.zbob.modules.products.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.products.dto.request.ProductRequest;
import com.zbob.modules.products.dto.response.ProductResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.dto.response.PublicProductResponse;
import com.zbob.modules.products.entity.ProductBadge;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductSummaryResponse> listPublished(String category, ProductBadge badge, Pageable pageable);

    PublicProductResponse getPublished(String slug);

    PageResponse<ProductSummaryResponse> listAll(String search, String category, Pageable pageable);

    ProductResponse get(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
