package com.zbob.modules.reports;

import java.time.LocalDate;
import java.util.List;

/**
 * Revenue counts orders that are paid, shipped or delivered. Order counts include orders still waiting for
 * payment. Cancelled orders count towards neither.
 */
public record SalesReport(
        int days,
        String timeZone,
        long orders,
        long revenueCents,
        long averageOrderCents,
        List<Day> daily,
        List<BestSeller> bestSellers,
        StockHealth stock) {

    public record Day(LocalDate date, long orders, long revenueCents) {
    }

    public record BestSeller(String productName, long units, long revenueCents) {
    }

    public record StockHealth(long inStock, long low, long out) {
    }
}
