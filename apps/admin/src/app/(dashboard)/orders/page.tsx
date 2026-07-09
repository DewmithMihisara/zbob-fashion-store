"use client";

import type { OrderStatus, OrderSummary, Page } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { ExportButton } from "@/components/ui/ExportButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { statusLabels, statusTones } from "@/features/orders/status";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

export default function OrdersPage() {
  const [status, setStatus] = useState<OrderStatus | "">("");
  const [page, setPage] = useState(0);
  const session = useSession();
  const remove = useAction();
  const { data, error, reload } = useData<Page<OrderSummary>>(`/admin/orders?page=${page}${status ? `&status=${status}` : ""}`);

  return (
    <>
      <PageHeader
        title="Orders"
        description="Order Now on the website is not connected to a payment gateway yet, so no orders are created from it. Only cancelled orders can be deleted."
        action={
          <span className="inline-flex items-start gap-2">
            {session?.role === "ADMIN" && <ExportButton name="orders" />}
            <Link href="/orders/new" className="inline-flex h-9 items-center rounded-lg bg-ink px-3 text-[13px] font-medium text-paper hover:bg-neutral-800">
              Add order
            </Link>
          </span>
        }
      />
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
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Order", "Customer", "Date", "Total", "Status", ""]}>
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
            <Cell className="text-right">
              <span className="inline-flex items-center gap-1">
                <Link href={`/orders/${order.id}`} className="inline-flex h-9 items-center rounded-lg border border-line bg-paper px-3 text-[13px] font-medium hover:bg-canvas">
                  Edit
                </Link>
                {session?.role === "ADMIN" && order.status === "CANCELLED" && (
                  <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/orders/${order.id}`, { method: "DELETE" })).then(reload)}>
                    Delete
                  </ConfirmButton>
                )}
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={6}>No orders yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
