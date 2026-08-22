package com.zbob.modules.returns;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    boolean existsByOrderIdAndStatusIn(Long orderId, Collection<ReturnStatus> statuses);

    Page<ReturnRequest> findByStatus(ReturnStatus status, Pageable pageable);
}
