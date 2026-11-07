package com.zbob.modules.shipping.repository;

import com.zbob.modules.shipping.entity.ShippingMethod;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {

    List<ShippingMethod> findByActiveTrueOrderByPriceCentsAsc();
}
