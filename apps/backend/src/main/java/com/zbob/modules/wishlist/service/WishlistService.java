package com.zbob.modules.wishlist.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.products.service.ProductService;
import com.zbob.modules.wishlist.entity.WishlistItem;
import com.zbob.modules.wishlist.repository.WishlistItemRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistItemRepository items;
    private final ProductRepository products;
    private final ProductService productService;

    public WishlistService(WishlistItemRepository items, ProductRepository products, ProductService productService) {
        this.items = items;
        this.products = products;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> list(Long userId, Pageable pageable) {
        return productService.summarize(items.findByUserIdAndProductPublishedTrue(userId, pageable).map(WishlistItem::getProduct));
    }

    @Transactional(readOnly = true)
    public List<Long> productIds(Long userId) {
        return items.findPublishedProductIds(userId);
    }

    /** Saving twice is harmless, so a double click never shows an error. */
    @Transactional
    public void add(Long userId, Long productId) {
        Product product = products.findById(productId)
                .filter(Product::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!items.existsByUserIdAndProductId(userId, productId)) {
            items.save(new WishlistItem(userId, product));
        }
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        items.deleteByUserIdAndProductId(userId, productId);
    }
}
