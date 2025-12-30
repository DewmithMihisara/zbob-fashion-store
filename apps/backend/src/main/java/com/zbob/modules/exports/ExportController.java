package com.zbob.modules.exports;

import com.zbob.exception.ResourceNotFoundException;
import com.zbob.modules.contact.entity.NewsletterSubscriber;
import com.zbob.modules.contact.repository.NewsletterSubscriberRepository;
import com.zbob.modules.inventory.entity.ProductVariant;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.repository.OrderRepository;
import com.zbob.modules.products.entity.Product;
import com.zbob.modules.products.repository.ProductRepository;
import com.zbob.modules.users.entity.Role;
import com.zbob.modules.users.entity.User;
import com.zbob.modules.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Downloads data as CSV, read in pages so a large table never sits in memory at once. Product and stock
 * exports are open to all staff; the ones with personal data need the admin role.
 */
@RestController
@RequestMapping("/api/v1/admin/exports")
public class ExportController {

    private static final int BATCH = 500;
    private static final Set<String> ADMIN_ONLY = Set.of("customers", "orders", "subscribers");

    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final UserRepository users;
    private final OrderRepository orders;
    private final NewsletterSubscriberRepository subscribers;

    public ExportController(ProductRepository products, ProductVariantRepository variants, UserRepository users,
            OrderRepository orders, NewsletterSubscriberRepository subscribers) {
        this.products = products;
        this.variants = variants;
        this.users = users;
        this.orders = orders;
        this.subscribers = subscribers;
    }

    @GetMapping("/{name}.csv")
    @Transactional(readOnly = true)
    public void export(@PathVariable String name, HttpServletResponse response) throws IOException {
        if (!List.of("products", "inventory", "customers", "orders", "subscribers").contains(name)) {
            throw new ResourceNotFoundException("Export", name);
        }
        if (ADMIN_ONLY.contains(name) && !isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException("Admins only");
        }
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"zbob-%s-%s.csv\"".formatted(name, LocalDate.now(ZoneOffset.UTC)));
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Writer writer = response.getWriter();
        writer.write('﻿'); // lets Excel detect UTF-8, so names with accents display correctly
        CsvWriter csv = new CsvWriter(writer);
        switch (name) {
            case "products" -> {
                csv.row(List.of("id", "name", "slug", "category", "brand", "badge", "price", "original_price", "published", "created_at"));
                write(csv, products::findAll, Sort.by("id"), (Product p) -> Arrays.asList(p.getId(), p.getName(), p.getSlug(),
                        p.getCategory().getName(), p.getBrand() == null ? null : p.getBrand().getName(), p.getBadge(),
                        CsvWriter.money(p.getPriceCents()),
                        p.getCompareAtPriceCents() == null ? null : CsvWriter.money(p.getCompareAtPriceCents()),
                        p.isPublished(), p.getCreatedAt()));
            }
            case "inventory" -> {
                csv.row(List.of("variant_id", "product", "sku", "size", "colour", "in_stock", "low_stock_at"));
                write(csv, variants::findAllPaged, Sort.by("id"), (ProductVariant v) -> Arrays.asList(v.getId(),
                        v.getProduct().getName(), v.getSku(), v.getSize(), v.getColor(), v.getStockQuantity(), v.getLowStockThreshold()));
            }
            case "customers" -> {
                csv.row(List.of("id", "full_name", "email", "active", "joined"));
                write(csv, page -> users.findByRoleIn(List.of(Role.CUSTOMER), page), Sort.by("id"), (User u) -> Arrays.asList(
                        u.getId(), u.getFullName(), u.getEmail(), u.isActive(), u.getCreatedAt()));
            }
            case "orders" -> {
                csv.row(List.of("order_number", "status", "customer_id", "recipient", "city", "country", "subtotal",
                        "discount", "shipping", "total", "promotion_code", "placed_at"));
                write(csv, orders::findAll, Sort.by("id"), (Order o) -> Arrays.asList(o.getOrderNumber(), o.getStatus(),
                        o.getUserId(), o.getRecipient(), o.getCity(), o.getCountry(), CsvWriter.money(o.getSubtotalCents()),
                        CsvWriter.money(o.getDiscountCents()), CsvWriter.money(o.getShippingCents()),
                        CsvWriter.money(o.getTotalCents()), o.getPromotionCode(), o.getCreatedAt()));
            }
            default -> {
                csv.row(List.of("email", "subscribed_at"));
                write(csv, subscribers::findAll, Sort.by("id"), (NewsletterSubscriber s) -> Arrays.asList(s.getEmail(), s.getCreatedAt()));
            }
        }
        writer.flush();
    }

    private static <T> void write(CsvWriter csv, Function<Pageable, Page<T>> source, Sort sort, Function<T, List<?>> toRow)
            throws IOException {
        Set<Object> seen = new HashSet<>();
        Page<T> page = source.apply(PageRequest.of(0, BATCH, sort));
        while (true) {
            for (T item : page.getContent()) {
                if (seen.add(item)) {
                    csv.row(toRow.apply(item));
                }
            }
            if (!page.hasNext()) {
                return;
            }
            page = source.apply(page.nextPageable());
        }
    }

    private static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
