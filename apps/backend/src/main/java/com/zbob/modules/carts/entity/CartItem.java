package com.zbob.modules.carts.entity;

import com.zbob.common.entity.BaseEntity;
import com.zbob.modules.inventory.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItem extends BaseEntity {

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {
    }

    public CartItem(Long cartId, ProductVariant variant, int quantity) {
        this.cartId = cartId;
        this.variant = variant;
        this.quantity = quantity;
    }

    public Long getCartId() {
        return cartId;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
