package com.zbob.modules.categories.service.impl;

import com.zbob.common.util.Slugs;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.categories.dto.request.CategoryRequest;
import com.zbob.modules.categories.dto.response.CategoryResponse;
import com.zbob.modules.categories.entity.Category;
import com.zbob.modules.categories.mapper.CategoryMapper;
import com.zbob.modules.categories.repository.CategoryRepository;
import com.zbob.modules.categories.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categories;

    public CategoryServiceImpl(CategoryRepository categories) {
        this.categories = categories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categories.findAllByOrderByPositionAscNameAsc().stream().map(CategoryMapper::toResponse).toList();
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
        categories.delete(find(id));
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
