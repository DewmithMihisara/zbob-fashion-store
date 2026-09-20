package com.zbob.modules.reports;

import com.zbob.config.ReportProperties;
import com.zbob.exception.BadRequestException;
import com.zbob.modules.inventory.repository.ProductVariantRepository;
import com.zbob.modules.orders.entity.Order;
import com.zbob.modules.orders.entity.OrderStatus;
import com.zbob.modules.orders.repository.OrderItemRepository;
import com.zbob.modules.orders.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private static final Set<OrderStatus> PAID = Set.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final ProductVariantRepository variants;
    private final ZoneId zone;
    private final Clock clock;

    public ReportService(OrderRepository orders, OrderItemRepository items, ProductVariantRepository variants,
            ReportProperties properties) {
        this(orders, items, variants, ZoneId.of(properties.timeZone()), Clock.systemUTC());
    }

    ReportService(OrderRepository orders, OrderItemRepository items, ProductVariantRepository variants, ZoneId zone, Clock clock) {
        this.orders = orders;
        this.items = items;
        this.variants = variants;
        this.zone = zone;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SalesReport sales(int days) {
        if (days < 1 || days > 366) {
            throw new BadRequestException("Choose between 1 and 366 days");
        }
        LocalDate today = LocalDate.now(clock.withZone(zone));
        LocalDate first = today.minusDays(days - 1L);
        var since = first.atStartOfDay(zone).toInstant();

        // Every day appears, so the chart shows quiet days as zero rather than skipping them.
        Map<LocalDate, long[]> byDay = new LinkedHashMap<>();
        for (LocalDate d = first; !d.isAfter(today); d = d.plusDays(1)) {
            byDay.put(d, new long[2]);
        }
        long orderCount = 0;
        long revenue = 0;
        long paidOrders = 0;
        for (Order order : orders.findByCreatedAtGreaterThanEqualAndStatusNot(since, OrderStatus.CANCELLED)) {
            long[] day = byDay.get(LocalDate.ofInstant(order.getCreatedAt(), zone));
            if (day == null) {
                continue;
            }
            day[0]++;
            orderCount++;
            if (PAID.contains(order.getStatus())) {
                day[1] += order.getTotalCents();
                revenue += order.getTotalCents();
                paidOrders++;
            }
        }
        List<SalesReport.Day> daily = new ArrayList<>();
        byDay.forEach((date, totals) -> daily.add(new SalesReport.Day(date, totals[0], totals[1])));

        List<SalesReport.BestSeller> best = items.findBestSellersSince(since, PageRequest.of(0, 5)).stream()
                .map(b -> new SalesReport.BestSeller(b.getProductName(), b.getUnits(), b.getRevenueCents()))
                .toList();

        long out = variants.countByStockQuantity(0);
        long low = variants.countLowStock() - out;
        long inStock = variants.count() - low - out;
        return new SalesReport(days, zone.getId(), orderCount, revenue, paidOrders == 0 ? 0 : revenue / paidOrders, daily, best,
                new SalesReport.StockHealth(inStock, low, out));
    }
}
