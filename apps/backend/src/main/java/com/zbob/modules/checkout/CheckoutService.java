package com.zbob.modules.checkout;

import com.zbob.exception.BadRequestException;
import com.zbob.modules.carts.repository.CartItemRepository;
import com.zbob.modules.carts.repository.CartRepository;
import com.zbob.modules.checkout.dto.CheckoutQuoteRequest;
import com.zbob.modules.checkout.dto.CheckoutQuoteResponse;
import com.zbob.modules.customers.repository.CustomerAddressRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.payments.service.PaymentService;
import com.zbob.modules.promotions.service.PromotionService;
import com.zbob.modules.shipping.entity.ShippingMethod;
import com.zbob.modules.shipping.repository.ShippingMethodRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CheckoutService {

    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductVariantRepository variants;
    private final CustomerAddressRepository addresses;
    private final ShippingMethodRepository shippingMethods;
    private final PromotionService promotions;
    private final PaymentService payments;

    public CheckoutService(CartRepository carts, CartItemRepository cartItems, ProductVariantRepository variants,
            CustomerAddressRepository addresses, ShippingMethodRepository shippingMethods, PromotionService promotions,
            PaymentService payments) {
        this.carts = carts;
        this.cartItems = cartItems;
        this.variants = variants;
        this.addresses = addresses;
        this.shippingMethods = shippingMethods;
        this.promotions = promotions;
        this.payments = payments;
    }

    /** Works out the totals. Problems are reported in the answer, so the page can show them all at once. */
    @Transactional(readOnly = true)
    public CheckoutQuoteResponse quote(Long userId, CheckoutQuoteRequest request) {
        List<CheckoutQuoteResponse.Line> lines = request.buyNow() != null ? buyNowLine(request.buyNow()) : cartLines(userId);
        List<String> problems = new ArrayList<>();
        if (lines.isEmpty()) {
            problems.add("There is nothing to check out.");
        }
        if (lines.stream().anyMatch(line -> !line.available())) {
            problems.add("Some items do not have enough stock. Lower the quantity or remove them.");
        }
        int subtotal = lines.stream().mapToInt(CheckoutQuoteResponse.Line::lineTotalCents).sum();

        Long addressId = null;
        if (request.addressId() != null) {
            addressId = addresses.findByIdAndUserId(request.addressId(), userId)
                    .orElseThrow(() -> new BadRequestException("Unknown delivery address"))
                    .getId();
        } else {
            problems.add("Choose a delivery address.");
        }

        ShippingMethod shipping = null;
        if (request.shippingMethodId() != null) {
            shipping = shippingMethods.findById(request.shippingMethodId())
                    .filter(ShippingMethod::isActive)
                    .orElseThrow(() -> new BadRequestException("Unknown delivery method"));
        } else {
            problems.add("Choose a delivery option.");
        }

        int discount = 0;
        String code = null;
        String promotionError = null;
        if (StringUtils.hasText(request.promotionCode())) {
            var found = promotions.findDiscount(request.promotionCode().trim(), subtotal);
            if (found.isPresent()) {
                discount = found.getAsInt();
                code = request.promotionCode().trim().toUpperCase(Locale.ROOT);
            } else {
                promotionError = PromotionService.NOT_USABLE;
            }
        }

        int shippingCents = shipping == null ? 0 : shipping.getPriceCents();
        boolean paymentAvailable = payments.isAvailable();
        return new CheckoutQuoteResponse(lines, subtotal, discount, shippingCents, subtotal - discount + shippingCents, code,
                promotionError, shipping == null ? null : shipping.getName(), addressId, paymentAvailable,
                problems.isEmpty() && paymentAvailable, problems);
    }

    private List<CheckoutQuoteResponse.Line> buyNowLine(CheckoutQuoteRequest.BuyNow buyNow) {
        ProductVariant variant = variants.findById(buyNow.variantId())
                .filter(v -> v.getProduct().isPublished())
                .orElseThrow(() -> new BadRequestException("This item is not available"));
        return List.of(line(variant, buyNow.quantity()));
    }

    private List<CheckoutQuoteResponse.Line> cartLines(Long userId) {
        return carts.findByUserId(userId)
                .map(cart -> cartItems.findByCartIdOrderByIdAsc(cart.getId()).stream()
                        .map(item -> line(item.getVariant(), item.getQuantity()))
                        .toList())
                .orElse(List.of());
    }

    private static CheckoutQuoteResponse.Line line(ProductVariant v, int quantity) {
        int price = v.getProduct().getPriceCents();
        return new CheckoutQuoteResponse.Line(v.getId(), v.getProduct().getName(), v.getProduct().getSlug(), v.getSize(),
                v.getColor(), quantity, price, price * quantity, v.getProduct().isPublished() && v.getStockQuantity() >= quantity);
    }
}
