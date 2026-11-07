"use client";

import type { DashboardSummary } from "@zbob/shared-types";
import Link from "next/link";
import { ErrorAlert } from "@/components/ui/Alert";
import { PageHeader } from "@/components/ui/PageHeader";
import { useData } from "@/hooks/useData";

export default function DashboardPage() {
  const { data, error } = useData<DashboardSummary>("/admin/dashboard");

  const cards = data
    ? [
        { label: "Products", value: data.products, note: `${data.publishedProducts} published`, href: "/products", alert: false },
        { label: "Low stock", value: data.lowStockVariants, note: "sizes at or below their limit", href: "/inventory?low=1", alert: data.lowStockVariants > 0 },
        { label: "Out of stock", value: data.outOfStockVariants, note: "sizes with none left", href: "/inventory?low=1", alert: data.outOfStockVariants > 0 },
        { label: "Reviews to approve", value: data.pendingReviews, note: "waiting for you", href: "/reviews", alert: data.pendingReviews > 0 },
        { label: "New messages", value: data.unhandledMessages, note: "from the contact form", href: "/messages", alert: data.unhandledMessages > 0 },
        { label: "Orders awaiting payment", value: data.ordersAwaitingPayment, note: "online ordering is off", href: "/orders", alert: false },
        { label: "Customers", value: data.customers, note: "registered accounts", href: "/customers", alert: false },
      ]
    : [];

  return (
    <>
      <PageHeader title="Dashboard" description="What needs your attention in the store." />
      <ErrorAlert message={error} />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <Link key={card.label} href={card.href} className="rounded-xl border border-line bg-paper p-5 transition-colors hover:border-faint">
            <p className="text-[13px] text-muted">{card.label}</p>
            <p className={`mt-1 text-[32px] font-semibold tracking-tight ${card.alert ? "text-accent" : ""}`}>{card.value}</p>
            <p className="text-[12px] text-muted">{card.note}</p>
          </Link>
        ))}
      </div>
    </>
  );
}
