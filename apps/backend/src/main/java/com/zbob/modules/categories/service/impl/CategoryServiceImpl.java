package com.zbob.modules.categories.service.impl;

import com.zbob.common.dto.PageResponse;
import com.zbob.common.util.Slugs;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.categories.dto.request.CategoryRequest;
import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.categories.dto.response.CategoryWithPricesResponse;
import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.mapper.CategoryMapper;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.categories.service.CategoryService;
import com.zbob.modules.products.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    public CategoryServiceImpl(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categories.findAllByOrderByPositionAscNameAsc().stream().map(CategoryMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithPricesResponse> listWithPrices() {
        Map<Long, ProductRepository.CategoryPriceRange> ranges = products.findPublishedPriceRanges().stream()
                .collect(Collectors.toMap(ProductRepository.CategoryPriceRange::getCategoryId, Function.identity()));
        return categories.findAllByOrderByPositionAscNameAsc().stream().map(c -> {
            var range = ranges.get(c.getId());
            return new CategoryWithPricesResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getBadge(),
                    range == null ? null : range.getMinCents(), range == null ? null : range.getMaxCents());
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> listPaged(Pageable pageable) {
        return PageResponse.of(categories.findAll(pageable), CategoryMapper::toResponse);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = Slugs.of(request.name());
        if (categories.existsBySlug(slug)) {
            throw new ConflictException("A category with this name already exists");
        }
        Category category = new Category(request.name().trim(), slug);
        apply(category, request);
        return CategoryMapper.toResponse(categories.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = find(id);
        String slug = Slugs.of(request.name());
        if (categories.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("A category with this name already exists");
        }
        category.setName(request.name().trim());
        category.setSlug(slug);
        apply(category, request);
        return CategoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = find(id);
        if (products.existsByCategoryId(id)) {
            throw new ConflictException("Move or delete this category's products first");
        }
        categories.delete(category);
    }

    private Category find(Long id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private static void apply(Category category, CategoryRequest request) {
        category.setDescription(request.description());
        category.setBadge(request.badge());
        category.setPosition(request.position());
    }
}
