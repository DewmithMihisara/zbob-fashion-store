package com.zbob.modules.brands.service.impl;

import com.zbob.common.dto.PageResponse;
import com.zbob.common.util.Slugs;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.brands.dto.request.BrandRequest;
import com.zbob.modules.brands.dto.response.BrandResponse;
import com.zbob.modules.brands.entity.Brand;
import com.zbob.modules.brands.mapper.BrandMapper;
import com.zbob.modules.brands.repository.BrandRepository;
import com.zbob.modules.brands.service.BrandService;
import com.zbob.modules.products.repository.ProductRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brands;
    private final ProductRepository products;

    public BrandServiceImpl(BrandRepository brands, ProductRepository products) {
        this.brands = brands;
        this.products = products;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> list() {
        return brands.findAllByOrderByNameAsc().stream().map(BrandMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> listPaged(Pageable pageable) {
        return PageResponse.of(brands.findAll(pageable), BrandMapper::toResponse);
    }

    @Override
    @Transactional
    public BrandResponse create(BrandRequest request) {
        String slug = Slugs.of(request.name());
        if (brands.existsBySlug(slug)) {
            throw new ConflictException("A brand with this name already exists");
        }
        return BrandMapper.toResponse(brands.save(new Brand(request.name().trim(), slug)));
    }

    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = find(id);
        String slug = Slugs.of(request.name());
        if (brands.existsBySlugAndIdNot(slug, id)) {
            throw new ConflictException("A brand with this name already exists");
        }
        brand.setName(request.name().trim());
        brand.setSlug(slug);
        return BrandMapper.toResponse(brand);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Brand brand = find(id);
        if (products.existsByBrandId(id)) {
            throw new ConflictException("Move or delete this brand's products first");
        }
        brands.delete(brand);
    }

    private Brand find(Long id) {
        return brands.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand", id));
    }
}
