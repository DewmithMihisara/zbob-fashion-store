package com.zbob.modules.orders.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Product details are copied here, so the order stays accurate if the product later changes or is deleted. */
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 20)
    private String size;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(name = "unit_price_cents", nullable = false)
    private int unitPriceCents;

    @Column(nullable = false)
    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(Long orderId, Long variantId, String productName, String sku, String size, String color,
            int unitPriceCents, int quantity) {
        this.orderId = orderId;
        this.variantId = variantId;
        this.productName = productName;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.unitPriceCents = unitPriceCents;
        this.quantity = quantity;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public int getUnitPriceCents() {
        return unitPriceCents;
    }

    public int getQuantity() {
        return quantity;
    }
}
