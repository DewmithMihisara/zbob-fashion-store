package com.zbob.modules.shipping.service;

import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.shipping.dto.ShippingMethodRequest;
import com.zbob.modules.shipping.dto.ShippingMethodResponse;
import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingService {

    private final ShippingMethodRepository methods;

    public ShippingService(ShippingMethodRepository methods) {
        this.methods = methods;
    }

    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> listActive() {
        return methods.findByActiveTrueOrderByPriceCentsAsc().stream().map(ShippingService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> listAll() {
        return methods.findAllByOrderByNameAsc().stream().map(ShippingService::toResponse).toList();
    }

    @Transactional
    public ShippingMethodResponse create(ShippingMethodRequest request) {
        ShippingMethod method = new ShippingMethod(request.name().trim(), request.priceCents(), request.estimatedDays());
        apply(method, request);
        return toResponse(methods.save(method));
    }

    @Transactional
    public ShippingMethodResponse update(Long id, ShippingMethodRequest request) {
        ShippingMethod method = find(id);
        method.setName(request.name().trim());
        apply(method, request);
        return toResponse(method);
    }

    @Transactional
    public void delete(Long id) {
        methods.delete(find(id));
    }

    private ShippingMethod find(Long id) {
        return methods.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipping method", id));
    }

    private static void apply(ShippingMethod m, ShippingMethodRequest r) {
        m.setDescription(r.description());
        m.setPriceCents(r.priceCents());
        m.setEstimatedDays(r.estimatedDays());
        m.setActive(r.active());
    }

    private static ShippingMethodResponse toResponse(ShippingMethod m) {
        return new ShippingMethodResponse(m.getId(), m.getName(), m.getDescription(), m.getPriceCents(),
                m.getEstimatedDays(), m.isActive());
    }
}
