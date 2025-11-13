"use client";

import type { OrderStatus, OrderSummary, Page } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { statusLabels, statusTones } from "@/features/orders/status";
import { useData } from "@/hooks/useData";

export default function OrdersPage() {
  const [status, setStatus] = useState<OrderStatus | "">("");
  const [page, setPage] = useState(0);
  const { data, error } = useData<Page<OrderSummary>>(`/admin/orders?page=${page}${status ? `&status=${status}` : ""}`);

  return (
    <>
      <PageHeader title="Orders" description="Order Now on the website is not connected to a payment gateway yet, so no orders are created from it." />
      <select
        aria-label="Filter by status"
        value={status}
        onChange={(e) => {
          setStatus(e.target.value as OrderStatus | "");
          setPage(0);
        }}
        className="mb-4 h-9 rounded-lg border border-line bg-paper px-3 text-[13px] outline-none focus:border-ink"
      >
        <option value="">All statuses</option>
        {Object.entries(statusLabels).map(([value, label]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
      <ErrorAlert message={error} />
      <Table headers={["Order", "Customer", "Date", "Total", "Status"]}>
        {data?.content.map((order) => (
          <tr key={order.id} className="hover:bg-canvas">
            <Cell>
              <Link href={`/orders/${order.id}`} className="font-medium hover:underline">
                {order.orderNumber}
              </Link>
            </Cell>
            <Cell>{order.recipient}</Cell>
            <Cell>{new Date(order.createdAt).toLocaleDateString()}</Cell>
            <Cell>{formatPrice(order.totalCents)}</Cell>
            <Cell>
              <StatusBadge tone={statusTones[order.status]}>{statusLabels[order.status]}</StatusBadge>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No orders yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
