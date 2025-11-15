"use client";

import type { OrderDetail } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { use } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { PageHeader } from "@/components/ui/PageHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, Table } from "@/components/ui/Table";
import { nextStatuses, statusLabels, statusTones } from "@/features/orders/status";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export default function OrderPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: order, error, reload } = useData<OrderDetail>(`/admin/orders/${id}`);
  const action = useAction();

  return (
    <div className="max-w-[860px]">
      <Link href="/orders" className="text-[13px] text-muted hover:underline">
        Back to orders
      </Link>
      <PageHeader title={order?.orderNumber ?? "Order"} action={order && <StatusBadge tone={statusTones[order.status]}>{statusLabels[order.status]}</StatusBadge>} />
      <ErrorAlert message={error ?? action.error} />
      {order && (
        <div className="flex flex-col gap-5">
          <Table headers={["Item", "SKU", "Options", "Price", "Qty"]}>
            {order.items.map((item) => (
              <tr key={item.id}>
                <Cell className="font-medium">{item.productName}</Cell>
                <Cell>{item.sku}</Cell>
                <Cell>
                  {item.size} / {item.color}
                </Cell>
                <Cell>{formatPrice(item.unitPriceCents)}</Cell>
                <Cell>{item.quantity}</Cell>
              </tr>
            ))}
          </Table>
          <div className="grid gap-5 sm:grid-cols-2">
            <section className="rounded-xl border border-line bg-paper p-5 text-[13px] leading-6">
              <h2 className="mb-2 text-[15px] font-semibold">Deliver to</h2>
              <p>{order.recipient}</p>
              <p>{order.line1}</p>
              {order.line2 && <p>{order.line2}</p>}
              <p>
                {order.city}
                {order.region ? `, ${order.region}` : ""} {order.postalCode}
              </p>
              <p>{order.country}</p>
              {order.phone && <p>{order.phone}</p>}
              <p className="mt-2 text-muted">{order.shippingMethod}</p>
            </section>
            <section className="rounded-xl border border-line bg-paper p-5 text-[13px] leading-6">
              <h2 className="mb-2 text-[15px] font-semibold">Totals</h2>
              <p className="flex justify-between">
                <span>Subtotal</span> {formatPrice(order.subtotalCents)}
              </p>
              <p className="flex justify-between">
                <span>Discount{order.promotionCode ? ` (${order.promotionCode})` : ""}</span> -{formatPrice(order.discountCents)}
              </p>
              <p className="flex justify-between">
                <span>Shipping</span> {formatPrice(order.shippingCents)}
              </p>
              <p className="mt-1 flex justify-between border-t border-line pt-1 font-semibold">
                <span>Total</span> {formatPrice(order.totalCents)}
              </p>
            </section>
          </div>
          {nextStatuses[order.status].length > 0 && (
            <div className="flex flex-wrap gap-2">
              {nextStatuses[order.status].map((status) => (
                <Button
                  key={status}
                  variant={status === "CANCELLED" ? "danger" : "primary"}
                  disabled={action.busy}
                  onClick={() => action.run(() => api(`/admin/orders/${id}/status`, { method: "PATCH", json: { status } })).then(reload)}
                >
                  Mark as {statusLabels[status].toLowerCase()}
                </Button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
