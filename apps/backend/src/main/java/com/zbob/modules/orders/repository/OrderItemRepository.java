package com.zbob.modules.orders.repository;

import com.zbob.modules.orders.entity.OrderItem;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

    Page<OrderItem> findByOrderId(Long orderId, Pageable pageable);

    /** Units and revenue per product name since a time, leaving out cancelled orders. */
    @Query("""
            select i.productName as productName, sum(i.quantity) as units, sum(i.unitPriceCents * i.quantity) as revenueCents
            from OrderItem i, Order o
            where i.orderId = o.id and o.createdAt >= :since and o.status <> com.zbob.modules.orders.entity.OrderStatus.CANCELLED
            group by i.productName
            order by sum(i.quantity) desc, i.productName""")
    List<BestSeller> findBestSellersSince(Instant since, Pageable pageable);

    interface BestSeller {
        String getProductName();

        Long getUnits();

        Long getRevenueCents();
    }
}
