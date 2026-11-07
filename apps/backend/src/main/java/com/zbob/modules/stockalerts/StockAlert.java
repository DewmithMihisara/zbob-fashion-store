package com.zbob.modules.stockalerts;

import com.zbob.common.entity.BaseEntity;
import com.zbob.modules.inventory.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** A request to be emailed once when a sold out size and colour is back. */
@Entity
@Table(name = "stock_alerts")
public class StockAlert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private String email;

    @Column(name = "notified_at")
    private Instant notifiedAt;

    protected StockAlert() {
    }

    public StockAlert(ProductVariant variant, String email) {
        this.variant = variant;
        this.email = email;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public String getEmail() {
        return email;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void markNotified(Instant at) {
        this.notifiedAt = at;
    }
}
