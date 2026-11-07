package com.zbob.modules.dashboard;

import com.zbob.modules.contact.repository.ContactMessageRepository;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.reviews.repository.ReviewRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardController {

    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ReviewRepository reviews;
    private final ContactMessageRepository messages;
    private final OrderRepository orders;
    private final UserRepository users;

    public DashboardController(ProductRepository products, ProductVariantRepository variants, ReviewRepository reviews,
            ContactMessageRepository messages, OrderRepository orders, UserRepository users) {
        this.products = products;
        this.variants = variants;
        this.reviews = reviews;
        this.messages = messages;
        this.orders = orders;
        this.users = users;
    }

    @GetMapping
    @Transactional(readOnly = true)
    DashboardResponse summary() {
        return new DashboardResponse(
                products.count(),
                products.countByPublishedTrue(),
                variants.countLowStock(),
                variants.countByStockQuantity(0),
                reviews.countByApprovedFalse(),
                messages.countByHandledFalse(),
                orders.countByStatus(OrderStatus.PENDING_PAYMENT),
                users.countByRole(Role.CUSTOMER));
    }
}
