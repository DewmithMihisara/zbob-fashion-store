package com.zbob.modules.orders.repository;

import com.zbob.modules.orders.entity.OrderItem;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);
}
