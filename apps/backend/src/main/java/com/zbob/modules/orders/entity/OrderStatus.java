package com.zbob.modules.orders.entity;

import java.util.Set;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING_PAYMENT -> Set.of(PAID, CANCELLED).contains(next);
            case PAID -> Set.of(SHIPPED, CANCELLED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
