package com.zbob.modules.returns;

import com.zbob.common.dto.PageResponse;
import com.zbob.config.ReturnProperties;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.notifications.EmailService;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnService {

    public record ReturnResponse(Long id, Long orderId, String orderNumber, String customerName, String customerEmail,
            String reason, ReturnStatus status, String staffNote, Instant requestedAt, Instant decidedAt) {
    }

    private final ReturnRequestRepository returns;
    private final OrderRepository orders;
    private final UserRepository users;
    private final EmailService emails;
    private final int windowDays;

    public ReturnService(ReturnRequestRepository returns, OrderRepository orders, UserRepository users, EmailService emails,
            ReturnProperties properties) {
        this.returns = returns;
        this.orders = orders;
        this.users = users;
        this.emails = emails;
        this.windowDays = properties.windowDays();
    }

    /**
     * Only delivered orders, within the return window counted from when the order was marked delivered,
     * and only one open request per order.
     */
    @Transactional
    public ReturnResponse request(Long userId, Long orderId, String reason) {
        Order order = orders.findByIdAndUserId(orderId, userId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ConflictException("Returns can be requested once an order has been delivered");
        }
        if (order.getUpdatedAt().plus(Duration.ofDays(windowDays)).isBefore(Instant.now())) {
            throw new ConflictException("The " + windowDays + " day return window for this order has closed. Please contact us.");
        }
        if (returns.existsByOrderIdAndStatusIn(orderId, List.of(ReturnStatus.REQUESTED, ReturnStatus.APPROVED))) {
            throw new ConflictException("There is already a return request open for this order");
        }
        return toResponse(returns.save(new ReturnRequest(orderId, userId, reason.trim())), order, null);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> forCustomerOrder(Long userId, Long orderId) {
        Order order = orders.findByIdAndUserId(orderId, userId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return returns.findByOrderIdOrderByCreatedAtDesc(orderId).stream().map(r -> toResponse(r, order, null)).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReturnResponse> list(ReturnStatus status, Pageable pageable) {
        Page<ReturnRequest> page = status == null ? returns.findAll(pageable) : returns.findByStatus(status, pageable);
        Map<Long, Order> ordersById = orders.findAllById(page.getContent().stream().map(ReturnRequest::getOrderId).toList())
                .stream().collect(Collectors.toMap(Order::getId, Function.identity()));
        Map<Long, User> usersById = users.findAllById(page.getContent().stream().map(ReturnRequest::getUserId).toList())
                .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return PageResponse.of(page, r -> toResponse(r, ordersById.get(r.getOrderId()), usersById.get(r.getUserId())));
    }

    @Transactional
    public ReturnResponse decide(Long id, ReturnStatus next, String note) {
        ReturnRequest request = returns.findById(id).orElseThrow(() -> new ResourceNotFoundException("Return request", id));
        if (!request.getStatus().canMoveTo(next)) {
            throw new ConflictException("A return that is " + request.getStatus() + " can not become " + next);
        }
        request.decide(next, note == null || note.isBlank() ? null : note.trim(), Instant.now());
        Order order = orders.findById(request.getOrderId()).orElseThrow();
        User user = users.findById(request.getUserId()).orElseThrow();
        if (next != ReturnStatus.COMPLETED) {
            emails.returnDecided(user, order.getOrderNumber(), next == ReturnStatus.APPROVED ? "approved" : "declined", request.getStaffNote());
        }
        return toResponse(request, order, user);
    }

    private static ReturnResponse toResponse(ReturnRequest r, Order order, User user) {
        return new ReturnResponse(r.getId(), r.getOrderId(), order == null ? null : order.getOrderNumber(),
                user == null ? null : user.getFullName(), user == null ? null : user.getEmail(), r.getReason(), r.getStatus(),
                r.getStaffNote(), r.getCreatedAt(), r.getDecidedAt());
    }
}
