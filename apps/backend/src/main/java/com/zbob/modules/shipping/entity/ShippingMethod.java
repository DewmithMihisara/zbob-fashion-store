package com.zbob.modules.shipping.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipping_methods")
public class ShippingMethod extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "price_cents", nullable = false)
    private int priceCents;

    @Column(name = "estimated_days", nullable = false)
    private int estimatedDays;

    @Column(nullable = false)
    private boolean active = true;

    protected ShippingMethod() {
    }

    public ShippingMethod(String name, int priceCents, int estimatedDays) {
        this.name = name;
        this.priceCents = priceCents;
        this.estimatedDays = estimatedDays;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public void setPriceCents(int priceCents) {
        this.priceCents = priceCents;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(int estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
