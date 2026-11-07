package com.zbob.modules.orders.repository;

import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    long countByStatus(OrderStatus status);

    boolean existsByUserId(Long userId);

    List<Order> findByCreatedAtGreaterThanEqualAndStatusNot(Instant since, OrderStatus status);
}
