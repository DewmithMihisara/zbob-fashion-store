package com.zbob.modules.products.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.products.dto.request.ProductRequest;
import com.zbob.modules.products.dto.response.ProductResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.dto.response.PublicProductResponse;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    PageResponse<ProductSummaryResponse> listPublished(String category, ProductBadge badge, String search, Pageable pageable);

    /** Card details for a page of products, loaded without one query per product. */
    PageResponse<ProductSummaryResponse> summarize(Page<Product> page);

    PublicProductResponse getPublished(String slug);

    PageResponse<ProductSummaryResponse> listAll(String search, String category, Pageable pageable);

    ProductResponse get(Long id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
