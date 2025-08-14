package com.zbob.modules.products.mapper;

import com.zbob.modules.brands.mapper.BrandMapper;
import com.zbob.modules.categories.mapper.CategoryMapper;
import com.zbob.modules.inventory.dto.response.VariantResponse;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.products.dto.response.ProductImageResponse;
import com.zbob.modules.products.dto.response.ProductResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.dto.response.PublicProductResponse;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductImage;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
    }

    /** The card shows the first image large and up to two swatches for quick switching. */
    public static ProductSummaryResponse toSummary(Product p, List<String> imageUrls, boolean inStock) {
        return new ProductSummaryResponse(p.getId(), p.getName(), p.getSlug(), p.getBadge(), p.getPriceCents(),
                p.getCompareAtPriceCents(), imageUrls.isEmpty() ? null : imageUrls.get(0),
                imageUrls.stream().limit(2).toList(), p.getCategory().getSlug(), inStock, p.isPublished());
    }

    public static ProductResponse toResponse(Product p, List<ProductImage> images, List<ProductVariant> variants) {
        return new ProductResponse(p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getMaterial(), p.getCare(),
                p.getWarranty(), p.getBadge(), p.getPriceCents(), p.getCompareAtPriceCents(), p.isPublished(),
                variants.stream().anyMatch(v -> v.getStockQuantity() > 0),
                CategoryMapper.toResponse(p.getCategory()),
                p.getBrand() == null ? null : BrandMapper.toResponse(p.getBrand()),
                images.stream().map(ProductMapper::toImage).toList(),
                variants.stream().map(ProductMapper::toVariant).toList());
    }

    public static PublicProductResponse toPublic(Product p, List<ProductImage> images, boolean inStock) {
        return new PublicProductResponse(p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getMaterial(),
                p.getCare(), p.getWarranty(), p.getBadge(), p.getPriceCents(), p.getCompareAtPriceCents(), inStock,
                CategoryMapper.toResponse(p.getCategory()), images.stream().map(ProductMapper::toImage).toList());
    }

    public static ProductImageResponse toImage(ProductImage i) {
        return new ProductImageResponse(i.getId(), i.getUrl(), i.getAltText(), i.getPosition());
    }

    public static VariantResponse toVariant(ProductVariant v) {
        return new VariantResponse(v.getId(), v.getSku(), v.getSize(), v.getColor(), v.getStockQuantity(),
                v.getLowStockThreshold(), v.isLowStock());
    }
}
