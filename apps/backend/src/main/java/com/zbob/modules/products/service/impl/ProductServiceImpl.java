package com.zbob.modules.products.service.impl;

import static com.zbob.modules.products.specification.ProductSpecification.hasBadge;
import static com.zbob.modules.products.specification.ProductSpecification.inCategory;
import static com.zbob.modules.products.specification.ProductSpecification.nameContains;
import static com.zbob.modules.products.specification.ProductSpecification.published;

import com.zbob.common.dto.PageResponse;
import com.zbob.common.util.Slugs;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.brands.repository.BrandRepository;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.products.dto.request.ProductRequest;
import com.zbob.modules.products.dto.response.ProductResponse;
import com.zbob.modules.products.dto.response.ProductSummaryResponse;
import com.zbob.modules.products.dto.response.PublicProductResponse;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.entity.ProductBadge;
import com.zbob.modules.products.entity.ProductImage;
import com.zbob.modules.products.mapper.ProductMapper;
import com.zbob.modules.products.repository.ProductImageRepository;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.products.service.ProductImageService;
import com.zbob.modules.products.service.ProductService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository products;
    private final ProductImageRepository images;
    private final ProductVariantRepository variants;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductImageService imageService;

    public ProductServiceImpl(ProductRepository products, ProductImageRepository images,
            ProductVariantRepository variants, CategoryRepository categories, BrandRepository brands,
            ProductImageService imageService) {
        this.products = products;
        this.images = images;
        this.variants = variants;
        this.categories = categories;
        this.brands = brands;
        this.imageService = imageService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> listPublished(String category, ProductBadge badge, Pageable pageable) {
        Specification<Product> spec = published().and(inCategory(category)).and(hasBadge(badge));
        return summaries(products.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PublicProductResponse getPublished(String slug) {
        Product product = products.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", slug));
        boolean inStock = !variants.findProductIdsInStock(List.of(product.getId())).isEmpty();
        return ProductMapper.toPublic(product, images.findByProductIdOrderByPositionAscIdAsc(product.getId()), inStock);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> listAll(String search, String category, Pageable pageable) {
        Specification<Product> spec = nameContains(search).and(inCategory(category));
        return summaries(products.findAll(spec, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        String slug = Slugs.of(request.name());
        if (products.existsBySlug(slug)) {
            throw new ConflictException("A product with this name already exists");
        }
        Product product = new Product(request.name().trim(), slug, null);
        apply(product, request);
        return toResponse(products.save(product));
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        // The slug is kept on rename so links to the product keep working.
        apply(product, request);
        product.setName(request.name().trim());
        return toResponse(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        imageService.deleteFilesAfterCommit(id);
        products.delete(product);
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private void apply(Product product, ProductRequest request) {
        if (request.compareAtPriceCents() != null && request.compareAtPriceCents() < request.priceCents()) {
            throw new BadRequestException("The original price cannot be lower than the sale price");
        }
        product.setDescription(request.description());
        product.setMaterial(request.material());
        product.setCare(request.care());
        product.setWarranty(request.warranty());
        product.setBadge(request.badge());
        product.setPriceCents(request.priceCents());
        product.setCompareAtPriceCents(request.compareAtPriceCents());
        product.setPublished(request.published());
        product.setCategory(categories.findById(request.categoryId())
                .orElseThrow(() -> new BadRequestException("Unknown category: " + request.categoryId())));
        product.setBrand(request.brandId() == null ? null : brands.findById(request.brandId())
                .orElseThrow(() -> new BadRequestException("Unknown brand: " + request.brandId())));
    }

    private ProductResponse toResponse(Product product) {
        return ProductMapper.toResponse(product,
                images.findByProductIdOrderByPositionAscIdAsc(product.getId()),
                variants.findByProductIdOrderBySizeAscColorAsc(product.getId()));
    }

    /** Loads images and stock flags in one query each, so a page of cards is not N+1. */
    private PageResponse<ProductSummaryResponse> summaries(Page<Product> page) {
        List<Long> ids = page.getContent().stream().map(Product::getId).toList();
        Map<Long, List<String>> urls = new HashMap<>();
        for (ProductImage image : images.findByProductIdInOrderByPositionAscIdAsc(ids)) {
            urls.computeIfAbsent(image.getProduct().getId(), id -> new ArrayList<>()).add(image.getUrl());
        }
        Set<Long> inStock = new HashSet<>(variants.findProductIdsInStock(ids));
        return PageResponse.of(page, p -> ProductMapper.toSummary(
                p, urls.getOrDefault(p.getId(), List.of()), inStock.contains(p.getId())));
    }
}
