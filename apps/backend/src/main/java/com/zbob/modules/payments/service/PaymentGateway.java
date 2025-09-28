package com.zbob.modules.payments.service;

/**
 * Implement this for the chosen provider and register it as a bean to enable payments.
 * No implementation exists yet because zbob has not chosen a payment gateway.
 */
public interface PaymentGateway {

    /** Provider name saved on each payment, e.g. "stripe". */
    String name();

    /** Starts a payment and returns the provider's reference for it. */
    String startPayment(Long orderId, int amountCents);
}
