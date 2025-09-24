package com.zbob.modules.carts.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    protected Cart() {
    }

    public Cart(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
