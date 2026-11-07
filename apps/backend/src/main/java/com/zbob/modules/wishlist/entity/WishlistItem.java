package com.zbob.modules.wishlist.entity;

import com.zbob.common.entity.BaseEntity;
import com.zbob.modules.products.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlist_items")
public class WishlistItem extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected WishlistItem() {
    }

    public WishlistItem(Long userId, Product product) {
        this.userId = userId;
        this.product = product;
    }

    public Long getUserId() {
        return userId;
    }

    public Product getProduct() {
        return product;
    }
}
