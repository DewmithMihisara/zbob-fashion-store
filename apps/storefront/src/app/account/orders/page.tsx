"use client";

import type { OrderSummary, Page } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useState } from "react";
import { orderStatusLabels } from "@/components/account/order-status";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerData } from "@/hooks/useCustomerData";

export default function OrdersPage() {
  const [page, setPage] = useState(0);
  const { data, error } = useCustomerData<Page<OrderSummary>>(`/orders?page=${page}`);

  return (
    <div className="flex flex-col gap-3">
      <ErrorNotice message={error} />
      {data?.content.length === 0 && page === 0 && <p className="py-10 text-center text-muted">You have no orders yet.</p>}
      <ul className="flex flex-col gap-3">
        {data?.content.map((order) => (
          <li key={order.id}>
            <Link href={`/account/orders/${order.id}`} className="flex flex-wrap items-center justify-between gap-3 rounded-card bg-tile p-5 transition-colors hover:bg-line">
              <span>
                <span className="block text-[18px] font-medium tracking-tight-2">{order.orderNumber}</span>
                <span className="text-[15px] text-muted">{new Date(order.createdAt).toLocaleDateString()}</span>
              </span>
              <span className="text-right">
                <span className="block text-[18px] font-medium tracking-tight-2">{formatPrice(order.totalCents)}</span>
                <span className="text-[15px] text-muted">{orderStatusLabels[order.status]}</span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
      {data && (data.totalPages > 1 || page > 0) && (
        <nav aria-label="Order pages" className="mt-2 flex items-center justify-between text-[15px] text-muted">
          <span>
            Page {data.page + 1} of {Math.max(data.totalPages, 1)}
          </span>
          <span className="flex gap-2">
            <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)} className={buttonClass("light", "disabled:opacity-40")}>
              Previous
            </button>
            <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className={buttonClass("light", "disabled:opacity-40")}>
              Next
            </button>
          </span>
        </nav>
      )}
    </div>
  );
}
