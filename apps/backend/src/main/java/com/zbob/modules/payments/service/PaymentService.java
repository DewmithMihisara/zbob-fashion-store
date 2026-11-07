package com.zbob.modules.payments.service;

import com.zbob.exception.BadRequestException;
import com.zbob.exception.PaymentsNotConfiguredException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.payments.dto.PaymentResponse;
import com.zbob.modules.payments.entity.Payment;
import com.zbob.modules.payments.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final Optional<PaymentGateway> gateway;

    public PaymentService(PaymentRepository payments, OrderRepository orders, Optional<PaymentGateway> gateway) {
        this.payments = payments;
        this.orders = orders;
        this.gateway = gateway;
    }

    @Transactional
    public PaymentResponse start(Long userId, Long orderId) {
        PaymentGateway provider = gateway.orElseThrow(PaymentsNotConfiguredException::new);
        Order order = orders.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("This order is not waiting for payment");
        }
        Payment payment = new Payment(orderId, provider.name(), order.getTotalCents());
        payment.setReference(provider.startPayment(orderId, order.getTotalCents()));
        return toResponse(payments.save(payment));
    }

    public boolean isAvailable() {
        return gateway.isPresent();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> forOrder(Long orderId) {
        return payments.findByOrderIdOrderByIdDesc(orderId).stream().map(PaymentService::toResponse).toList();
    }

    private static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getProvider(), p.getStatus(), p.getAmountCents(), p.getReference());
    }
}
