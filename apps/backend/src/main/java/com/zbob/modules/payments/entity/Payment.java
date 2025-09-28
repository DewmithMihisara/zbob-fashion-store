package com.zbob.modules.payments.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** One attempt to pay an order. Card details never reach this system; they stay with the gateway. */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 40)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount_cents", nullable = false)
    private int amountCents;

    @Column(length = 120)
    private String reference;

    protected Payment() {
    }

    public Payment(Long orderId, String provider, int amountCents) {
        this.orderId = orderId;
        this.provider = provider;
        this.amountCents = amountCents;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProvider() {
        return provider;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
