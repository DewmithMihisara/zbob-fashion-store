package com.zbob.modules.promotions.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "promotions")
public class Promotion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Column(name = "minimum_cents", nullable = false)
    private int minimumCents;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(nullable = false)
    private boolean active = true;

    protected Promotion() {
    }

    public Promotion(String code, DiscountType discountType, int discountValue) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
    }

    public boolean isUsableAt(Instant now, int subtotalCents) {
        return active
                && subtotalCents >= minimumCents
                && (startsAt == null || !now.isBefore(startsAt))
                && (endsAt == null || now.isBefore(endsAt));
    }

    /** Never more than the subtotal, so an order total can not go below zero. */
    public int discountOn(int subtotalCents) {
        int discount = discountType == DiscountType.PERCENT
                ? Math.toIntExact(subtotalCents * (long) discountValue / 100)
                : discountValue;
        return Math.min(discount, subtotalCents);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(int discountValue) {
        this.discountValue = discountValue;
    }

    public int getMinimumCents() {
        return minimumCents;
    }

    public void setMinimumCents(int minimumCents) {
        this.minimumCents = minimumCents;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
