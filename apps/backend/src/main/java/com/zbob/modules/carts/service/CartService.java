package com.zbob.modules.carts.service;

import com.zbob.exception.BadRequestException;
import com.zbob.exception.ConflictException;
import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.carts.dto.AddCartItemRequest;
import com.zbob.modules.carts.dto.CartItemResponse;
import com.zbob.modules.carts.dto.CartResponse;
import com.zbob.modules.carts.entity.Cart;
import com.zbob.modules.carts.entity.CartItem;
import com.zbob.modules.carts.repository.CartItemRepository;
import com.zbob.modules.carts.repository.CartRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Holds items for a customer. Prices always come from the current product, never from the client. */
@Service
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductVariantRepository variants;

    public CartService(CartRepository carts, CartItemRepository items, ProductVariantRepository variants) {
        this.carts = carts;
        this.items = items;
        this.variants = variants;
    }

    @Transactional(readOnly = true)
    public CartResponse get(Long userId) {
        return carts.findByUserId(userId).map(cart -> toResponse(cart.getId())).orElse(new CartResponse(List.of(), 0));
    }

    @Transactional
    public CartResponse add(Long userId, AddCartItemRequest request) {
        ProductVariant variant = variants.findById(request.variantId())
                .filter(v -> v.getProduct().isPublished())
                .orElseThrow(() -> new BadRequestException("This item is not available"));
        Cart cart = carts.findByUserId(userId).orElseGet(() -> carts.save(new Cart(userId)));
        CartItem item = items.findByCartIdAndVariantId(cart.getId(), variant.getId()).orElse(null);
        int quantity = request.quantity() + (item == null ? 0 : item.getQuantity());
        requireStock(variant, quantity);
        if (item == null) {
            items.save(new CartItem(cart.getId(), variant, quantity));
        } else {
            item.setQuantity(quantity);
        }
        items.flush();
        return toResponse(cart.getId());
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = findCart(userId);
        CartItem item = findItem(cart, itemId);
        requireStock(item.getVariant(), quantity);
        item.setQuantity(quantity);
        return toResponse(cart.getId());
    }

    @Transactional
    public CartResponse remove(Long userId, Long itemId) {
        Cart cart = findCart(userId);
        items.delete(findItem(cart, itemId));
        items.flush();
        return toResponse(cart.getId());
    }

    private Cart findCart(Long userId) {
        return carts.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart", userId));
    }

    /** Looked up within the customer's own cart, so another customer's item is simply not found. */
    private CartItem findItem(Cart cart, Long itemId) {
        return items.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));
    }

    private static void requireStock(ProductVariant variant, int quantity) {
        if (variant.getStockQuantity() < quantity) {
            throw new ConflictException("Only " + variant.getStockQuantity() + " left in stock");
        }
    }

    private CartResponse toResponse(Long cartId) {
        List<CartItemResponse> lines = items.findByCartIdOrderByIdAsc(cartId).stream().map(item -> {
            ProductVariant v = item.getVariant();
            int price = v.getProduct().getPriceCents();
            return new CartItemResponse(item.getId(), v.getId(), v.getProduct().getName(), v.getProduct().getSlug(),
                    v.getSize(), v.getColor(), item.getQuantity(), price, price * item.getQuantity(),
                    v.getProduct().isPublished() && v.getStockQuantity() >= item.getQuantity());
        }).toList();
        return new CartResponse(lines, lines.stream().mapToInt(CartItemResponse::lineTotalCents).sum());
    }
}
