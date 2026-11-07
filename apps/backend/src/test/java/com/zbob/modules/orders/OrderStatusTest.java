package com.zbob.modules.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.zbob.modules.orders.entity.OrderStatus;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void followsTheNormalPath() {
        assertThat(OrderStatus.PENDING_PAYMENT.canMoveTo(OrderStatus.PAID)).isTrue();
        assertThat(OrderStatus.PAID.canMoveTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.DELIVERED)).isTrue();
    }

    @Test
    void canBeCancelledOnlyBeforeShipping() {
        assertThat(OrderStatus.PENDING_PAYMENT.canMoveTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PAID.canMoveTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canMoveTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void finishedOrdersCanNotChange() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(OrderStatus.DELIVERED.canMoveTo(next)).isFalse();
            assertThat(OrderStatus.CANCELLED.canMoveTo(next)).isFalse();
        }
    }

    @Test
    void cannotSkipPayment() {
        assertThat(OrderStatus.PENDING_PAYMENT.canMoveTo(OrderStatus.SHIPPED)).isFalse();
    }
}
