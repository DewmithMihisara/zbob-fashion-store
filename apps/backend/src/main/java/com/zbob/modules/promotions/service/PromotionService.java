package com.zbob.modules.promotions.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.promotions.dto.PromotionRequest;
import com.zbob.modules.promotions.dto.PromotionResponse;
import com.zbob.modules.promotions.entity.DiscountType;
import com.zbob.modules.promotions.entity.Promotion;
import com.zbob.modules.promotions.repository.PromotionRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.OptionalInt;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionService {

    private final PromotionRepository promotions;

    public PromotionService(PromotionRepository promotions) {
        this.promotions = promotions;
    }

    @Transactional(readOnly = true)
    public PageResponse<PromotionResponse> list(Pageable pageable) {
        return PageResponse.of(promotions.findAll(pageable), PromotionService::toResponse);
    }

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        validate(request);
        if (promotions.existsByCodeIgnoreCase(request.code())) {
            throw new ConflictException("This promotion code already exists");
        }
        Promotion promotion = new Promotion(normalize(request.code()), request.discountType(), request.discountValue());
        apply(promotion, request);
        return toResponse(promotions.save(promotion));
    }

    @Transactional
    public PromotionResponse update(Long id, PromotionRequest request) {
        validate(request);
        Promotion promotion = find(id);
        if (promotions.existsByCodeIgnoreCaseAndIdNot(request.code(), id)) {
            throw new ConflictException("This promotion code already exists");
        }
        promotion.setCode(normalize(request.code()));
        apply(promotion, request);
        return toResponse(promotion);
    }

    @Transactional
    public void delete(Long id) {
        promotions.delete(find(id));
    }

    public static final String NOT_USABLE = "This promotion code can not be used on this order";

    /** Discount in cents for an order, or an error explaining why the code can not be used. */
    @Transactional(readOnly = true)
    public int discountFor(String code, int subtotalCents) {
        return findDiscount(code, subtotalCents).orElseThrow(() -> new BadRequestException(NOT_USABLE));
    }

    /**
     * The discount when the code can be used, or empty. Callers that only want to show a message use this,
     * because an exception thrown inside a transaction marks the caller's whole transaction to roll back.
     */
    @Transactional(readOnly = true)
    public OptionalInt findDiscount(String code, int subtotalCents) {
        return promotions.findByCodeIgnoreCase(code)
                .filter(p -> p.isUsableAt(Instant.now(), subtotalCents))
                .map(p -> OptionalInt.of(p.discountOn(subtotalCents)))
                .orElse(OptionalInt.empty());
    }

    private Promotion find(Long id) {
        return promotions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Promotion", id));
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private static void validate(PromotionRequest r) {
        if (r.discountType() == DiscountType.PERCENT && r.discountValue() > 100) {
            throw new BadRequestException("A percentage discount can not be more than 100");
        }
        if (r.startsAt() != null && r.endsAt() != null && !r.endsAt().isAfter(r.startsAt())) {
            throw new BadRequestException("The end date must be after the start date");
        }
    }

    private static void apply(Promotion p, PromotionRequest r) {
        p.setDescription(r.description());
        p.setDiscountType(r.discountType());
        p.setDiscountValue(r.discountValue());
        p.setMinimumCents(r.minimumCents());
        p.setStartsAt(r.startsAt());
        p.setEndsAt(r.endsAt());
        p.setActive(r.active());
    }

    private static PromotionResponse toResponse(Promotion p) {
        return new PromotionResponse(p.getId(), p.getCode(), p.getDescription(), p.getDiscountType(), p.getDiscountValue(),
                p.getMinimumCents(), p.getStartsAt(), p.getEndsAt(), p.isActive());
    }
}
