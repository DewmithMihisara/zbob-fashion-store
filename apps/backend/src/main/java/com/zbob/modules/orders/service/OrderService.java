package com.zbob.modules.orders.service;

import com.zbob.common.dto.PageResponse;
import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.carts.entity.Cart;
import com.zbob.modules.carts.entity.CartItem;
import com.zbob.modules.carts.repository.CartItemRepository;
import com.zbob.modules.carts.repository.CartRepository;
import com.zbob.modules.customers.dto.AddressRequest;
import com.zbob.modules.customers.entity.CustomerAddress;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.dto.AdminCreateOrderRequest;
import com.zbob.modules.orders.dto.CreateOrderRequest;
import com.zbob.modules.orders.dto.OrderItemResponse;
import com.zbob.modules.orders.dto.OrderResponse;
import com.zbob.modules.orders.dto.OrderSummaryResponse;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderItem;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderItemRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.promotions.service.PromotionService;
import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Turns a cart into an order. The storefront does not call this yet: Order Now stays inert until a
 * payment gateway is chosen, and it is only here so that work can connect to a finished API.
 */
@Service
public class OrderService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final CustomerAddressRepository addresses;
    private final ShippingMethodRepository shippingMethods;
    private final ProductVariantRepository variants;
    private final PromotionService promotions;
    private final UserRepository users;

    public OrderService(OrderRepository orders, OrderItemRepository orderItems, CartRepository carts,
            CartItemRepository cartItems, CustomerAddressRepository addresses, ShippingMethodRepository shippingMethods,
            ProductVariantRepository variants, PromotionService promotions, UserRepository users) {
        this.orders = orders;
        this.orderItems = orderItems;
        this.carts = carts;
        this.cartItems = cartItems;
        this.addresses = addresses;
        this.shippingMethods = shippingMethods;
        this.variants = variants;
        this.promotions = promotions;
        this.users = users;
    }

    @Transactional
    public OrderResponse place(Long userId, CreateOrderRequest request) {
        Cart cart = carts.findByUserId(userId).orElseThrow(() -> new BadRequestException("Your cart is empty"));
        List<CartItem> lines = cartItems.findByCartIdOrderByIdAsc(cart.getId());
        if (lines.isEmpty()) {
            throw new BadRequestException("Your cart is empty");
        }
        CustomerAddress address = addresses.findByIdAndUserId(request.addressId(), userId)
                .orElseThrow(() -> new BadRequestException("Unknown delivery address"));
        ShippingMethod shipping = shippingMethods.findById(request.shippingMethodId())
                .filter(ShippingMethod::isActive)
                .orElseThrow(() -> new BadRequestException("Unknown delivery method"));

        int subtotal = lines.stream()
                .mapToInt(line -> line.getVariant().getProduct().getPriceCents() * line.getQuantity())
                .sum();
        boolean hasCode = StringUtils.hasText(request.promotionCode());
        int discount = hasCode ? promotions.discountFor(request.promotionCode().trim(), subtotal) : 0;

        Order order = new Order(newOrderNumber(), userId);
        order.setTotals(subtotal, discount, shipping.getPriceCents(),
                hasCode ? request.promotionCode().trim().toUpperCase(Locale.ROOT) : null, shipping.getName());
        order.setAddress(address.getRecipient(), address.getLine1(), address.getLine2(), address.getCity(),
                address.getRegion(), address.getPostalCode(), address.getCountry(), address.getPhone());
        orders.save(order);

        for (CartItem line : lines) {
            ProductVariant variant = line.getVariant();
            if (variants.reserveStock(variant.getId(), line.getQuantity()) == 0) {
                // Throwing rolls back the whole order, including stock already reserved for earlier lines.
                throw new ConflictException(variant.getProduct().getName() + " no longer has enough stock");
            }
            orderItems.save(new OrderItem(order.getId(), variant.getId(), variant.getProduct().getName(), variant.getSku(),
                    variant.getSize(), variant.getColor(), variant.getProduct().getPriceCents(), line.getQuantity()));
        }
        cartItems.deleteByCartId(cart.getId());
        return toResponse(order);
    }

    @Transactional
    public OrderResponse createForCustomer(AdminCreateOrderRequest request) {
        User customer = users.findById(request.customerId())
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .orElseThrow(() -> new BadRequestException("Unknown customer"));
        ShippingMethod shipping = shippingMethods.findById(request.shippingMethodId())
                .orElseThrow(() -> new BadRequestException("Unknown delivery method"));

        int subtotal = 0;
        List<ProductVariant> lineVariants = new ArrayList<>();
        for (AdminCreateOrderRequest.Line line : request.items()) {
            ProductVariant variant = variants.findById(line.variantId())
                    .orElseThrow(() -> new BadRequestException("Unknown item: " + line.variantId()));
            lineVariants.add(variant);
            subtotal += variant.getProduct().getPriceCents() * line.quantity();
        }
        boolean hasCode = StringUtils.hasText(request.promotionCode());
        int discount = hasCode ? promotions.discountFor(request.promotionCode().trim(), subtotal) : 0;

        Order order = new Order(newOrderNumber(), customer.getId());
        order.setTotals(subtotal, discount, shipping.getPriceCents(),
                hasCode ? request.promotionCode().trim().toUpperCase(Locale.ROOT) : null, shipping.getName());
        applyAddress(order, request.address());
        orders.save(order);

        for (int i = 0; i < lineVariants.size(); i++) {
            ProductVariant variant = lineVariants.get(i);
            int quantity = request.items().get(i).quantity();
            if (variants.reserveStock(variant.getId(), quantity) == 0) {
                throw new ConflictException(variant.getProduct().getName() + " does not have enough stock");
            }
            orderItems.save(new OrderItem(order.getId(), variant.getId(), variant.getProduct().getName(), variant.getSku(),
                    variant.getSize(), variant.getColor(), variant.getProduct().getPriceCents(), quantity));
        }
        return toResponse(order);
    }

    /** Delivery details can change until the order has shipped. */
    @Transactional
    public OrderResponse updateAddress(Long id, AddressRequest address) {
        Order order = find(id);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.PAID) {
            throw new ConflictException("The address can only change before the order ships");
        }
        applyAddress(order, address);
        return toResponse(order);
    }

    /** Only cancelled orders can go, because cancelling is what returns the items to stock. */
    @Transactional
    public void delete(Long id) {
        Order order = find(id);
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new ConflictException("Cancel the order before deleting it");
        }
        orders.delete(order);
    }

    private static void applyAddress(Order order, AddressRequest a) {
        order.setAddress(a.recipient(), a.line1(), a.line2(), a.city(), a.region(), a.postalCode(), a.country(), a.phone());
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listForCustomer(Long userId, Pageable pageable) {
        return PageResponse.of(orders.findByUserId(userId, pageable), OrderService::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponse getForCustomer(Long userId, Long orderId) {
        return toResponse(orders.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listAll(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null ? orders.findAll(pageable) : orders.findByStatus(status, pageable);
        return PageResponse.of(page, OrderService::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(find(id));
    }

    /** Cancelling returns the items to stock. */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus next) {
        Order order = find(id);
        if (!order.getStatus().canMoveTo(next)) {
            throw new ConflictException("An order that is " + order.getStatus() + " can not become " + next);
        }
        if (next == OrderStatus.CANCELLED) {
            for (OrderItem item : orderItems.findByOrderIdOrderByIdAsc(id)) {
                if (item.getVariantId() != null) {
                    variants.restoreStock(item.getVariantId(), item.getQuantity());
                }
            }
        }
        order.setStatus(next);
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderItemResponse> listItems(Long orderId, Pageable pageable) {
        find(orderId);
        return PageResponse.of(orderItems.findByOrderId(orderId, pageable), OrderService::toItem);
    }

    private static OrderItemResponse toItem(OrderItem i) {
        return new OrderItemResponse(i.getId(), i.getProductName(), i.getSku(), i.getSize(), i.getColor(),
                i.getUnitPriceCents(), i.getQuantity());
    }

    private Order find(Long id) {
        return orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private static String newOrderNumber() {
        byte[] bytes = new byte[6];
        RANDOM.nextBytes(bytes);
        return "ZB-" + HexFormat.of().withUpperCase().formatHex(bytes);
    }

    private static OrderSummaryResponse toSummary(Order o) {
        return new OrderSummaryResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalCents(), o.getRecipient(),
                o.getCreatedAt());
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = orderItems.findByOrderIdOrderByIdAsc(o.getId()).stream()
                .map(OrderService::toItem)
                .toList();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getSubtotalCents(), o.getDiscountCents(),
                o.getShippingCents(), o.getTotalCents(), o.getPromotionCode(), o.getShippingMethod(), o.getRecipient(),
                o.getLine1(), o.getLine2(), o.getCity(), o.getRegion(), o.getPostalCode(), o.getCountry(), o.getPhone(),
                o.getCreatedAt(), items);
    }
}
