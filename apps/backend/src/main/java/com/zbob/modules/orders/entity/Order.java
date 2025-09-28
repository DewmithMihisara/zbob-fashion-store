package com.zbob.modules.orders.entity;

import com.zbob.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Totals and the delivery address are copied onto the order, so later changes do not alter past orders. */
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "subtotal_cents", nullable = false)
    private int subtotalCents;

    @Column(name = "discount_cents", nullable = false)
    private int discountCents;

    @Column(name = "shipping_cents", nullable = false)
    private int shippingCents;

    @Column(name = "total_cents", nullable = false)
    private int totalCents;

    @Column(name = "promotion_code", length = 40)
    private String promotionCode;

    @Column(name = "shipping_method", nullable = false, length = 80)
    private String shippingMethod;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(nullable = false, length = 200)
    private String line1;

    @Column(length = 200)
    private String line2;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(length = 30)
    private String phone;

    protected Order() {
    }

    public Order(String orderNumber, Long userId) {
        this.orderNumber = orderNumber;
        this.userId = userId;
    }

    public void setTotals(int subtotal, int discount, int shipping, String promotionCode, String shippingMethod) {
        this.subtotalCents = subtotal;
        this.discountCents = discount;
        this.shippingCents = shipping;
        this.totalCents = subtotal - discount + shipping;
        this.promotionCode = promotionCode;
        this.shippingMethod = shippingMethod;
    }

    public void setAddress(String recipient, String line1, String line2, String city, String region, String postalCode,
            String country, String phone) {
        this.recipient = recipient;
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.phone = phone;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getSubtotalCents() {
        return subtotalCents;
    }

    public int getDiscountCents() {
        return discountCents;
    }

    public int getShippingCents() {
        return shippingCents;
    }

    public int getTotalCents() {
        return totalCents;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    public String getPhone() {
        return phone;
    }
}
