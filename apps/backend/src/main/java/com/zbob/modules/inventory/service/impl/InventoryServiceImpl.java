package com.zbob.modules.inventory.service.impl;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.inventory.dto.request.BulkStockRequest;
import com.zbob.modules.inventory.dto.request.StockUpdateRequest;
import com.zbob.modules.inventory.dto.request.VariantRequest;
import com.zbob.modules.inventory.dto.response.InventoryRowResponse;
import com.zbob.modules.inventory.dto.response.VariantResponse;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.inventory.service.InventoryService;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.mapper.ProductMapper;
import com.zbob.modules.products.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository variants;
    private final ProductRepository products;

    public InventoryServiceImpl(ProductVariantRepository variants, ProductRepository products) {
        this.variants = variants;
        this.products = products;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryRowResponse> list(boolean lowStockOnly, Pageable pageable) {
        Page<ProductVariant> page = lowStockOnly ? variants.findLowStock(pageable) : variants.findAllPaged(pageable);
        return PageResponse.of(page, v -> new InventoryRowResponse(v.getId(), v.getProduct().getId(),
                v.getProduct().getName(), v.getSku(), v.getSize(), v.getColor(), v.getStockQuantity(),
                v.getLowStockThreshold(), v.isLowStock(), v.getColorHex()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VariantResponse> listForProduct(Long productId, Pageable pageable) {
        if (!products.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return PageResponse.of(variants.findByProductId(productId, pageable), ProductMapper::toVariant);
    }

    @Override
    @Transactional
    public VariantResponse addVariant(Long productId, VariantRequest request) {
        Product product = products.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (variants.existsBySku(request.sku())) {
            throw new ConflictException("This SKU is already in use");
        }
        if (variants.existsByProductIdAndSizeAndColor(productId, request.size(), request.color())) {
            throw new ConflictException("This product already has that size and colour");
        }
        ProductVariant variant = new ProductVariant(
                product, request.sku(), request.size(), request.color(), request.stockQuantity());
        variant.setLowStockThreshold(request.lowStockThreshold());
        variant.setColorHex(normalizeHex(request.colorHex()));
        return ProductMapper.toVariant(variants.save(variant));
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long variantId, VariantRequest request) {
        ProductVariant variant = find(variantId);
        if (variants.existsBySkuAndIdNot(request.sku(), variantId)) {
            throw new ConflictException("This SKU is already in use");
        }
        if (variants.existsByProductIdAndSizeAndColorAndIdNot(
                variant.getProduct().getId(), request.size(), request.color(), variantId)) {
            throw new ConflictException("This product already has that size and colour");
        }
        variant.setSku(request.sku());
        variant.setSize(request.size());
        variant.setColor(request.color());
        variant.setStockQuantity(request.stockQuantity());
        variant.setLowStockThreshold(request.lowStockThreshold());
        variant.setColorHex(normalizeHex(request.colorHex()));
        return ProductMapper.toVariant(variant);
    }

    @Override
    @Transactional
    public VariantResponse updateStock(Long variantId, StockUpdateRequest request) {
        ProductVariant variant = find(variantId);
        variant.setStockQuantity(request.stockQuantity());
        return ProductMapper.toVariant(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        variants.delete(find(variantId));
    }

    @Override
    @Transactional
    public int bulkUpdateStock(BulkStockRequest request) {
        List<Long> ids = request.variantIds().stream().distinct().toList();
        List<ProductVariant> rows = variants.findAllById(ids);
        if (rows.size() != ids.size()) {
            throw new BadRequestException("Some of the chosen rows no longer exist. Reload the page and try again.");
        }
        if (request.mode() == BulkStockRequest.Mode.SET && request.amount() < 0) {
            throw new BadRequestException("Stock can not be set below 0");
        }
        List<String> wouldGoNegative = new ArrayList<>();
        for (ProductVariant row : rows) {
            int next = request.mode() == BulkStockRequest.Mode.SET ? request.amount() : row.getStockQuantity() + request.amount();
            if (next < 0) {
                wouldGoNegative.add(row.getSku());
            }
        }
        if (!wouldGoNegative.isEmpty()) {
            throw new ConflictException("This would take stock below 0 for: " + String.join(", ", wouldGoNegative));
        }
        for (ProductVariant row : rows) {
            row.setStockQuantity(request.mode() == BulkStockRequest.Mode.SET ? request.amount() : row.getStockQuantity() + request.amount());
        }
        return rows.size();
    }

    private static String normalizeHex(String hex) {
        return hex == null || hex.isBlank() ? null : hex.toUpperCase(java.util.Locale.ROOT);
    }

    private ProductVariant find(Long id) {
        return variants.findById(id).orElseThrow(() -> new ResourceNotFoundException("Variant", id));
    }
}
