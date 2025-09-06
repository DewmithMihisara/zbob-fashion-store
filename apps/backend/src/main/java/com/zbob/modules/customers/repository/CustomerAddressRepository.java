package com.zbob.modules.customers.repository;

import com.zbob.modules.customers.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByUserIdOrderByIdAsc(Long userId);

    Optional<CustomerAddress> findByIdAndUserId(Long id, Long userId);
}
