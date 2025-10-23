package com.zbob.modules.dashboard;

public record DashboardResponse(
        long products, long publishedProducts, long lowStockVariants, long outOfStockVariants, long pendingReviews,
        long unhandledMessages, long ordersAwaitingPayment, long customers) {
}
