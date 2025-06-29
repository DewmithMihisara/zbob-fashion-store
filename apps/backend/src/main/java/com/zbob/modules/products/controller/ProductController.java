package com.zbob.modules.products.controller;

import com.zbob.common.dto.PageResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.dto.response.PublicProductResponse;
import com.zbob.modules.products.entity.ProductBadge;
import com.zbob.modules.products.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    PageResponse<ProductSummaryResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ProductBadge badge,
            @PageableDefault(size = 24, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return products.listPublished(category, badge, pageable);
    }

    @GetMapping("/{slug}")
    PublicProductResponse get(@PathVariable String slug) {
        return products.getPublished(slug);
    }
}
