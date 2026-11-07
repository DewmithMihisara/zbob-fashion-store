"use client";

import type { OrderDetail } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { use } from "react";
import { OrderActions } from "@/components/account/OrderActions";
import { orderStatusLabels } from "@/components/account/order-status";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerData } from "@/hooks/useCustomerData";

export default function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: order, error, reload } = useCustomerData<OrderDetail>(`/orders/${encodeURIComponent(id)}`);

  return (
    <div className="flex flex-col gap-4">
      <Link href="/account/orders" className="text-[15px] text-muted underline">
        Back to orders
      </Link>
      <ErrorNotice message={error} />
      {order && (
        <>
          <div className="flex flex-wrap items-baseline justify-between gap-3">
            <h2 className="text-[26px] font-medium tracking-tight-3">{order.orderNumber}</h2>
            <p className="text-[16px] text-muted">
              {new Date(order.createdAt).toLocaleDateString()} · {orderStatusLabels[order.status]}
            </p>
          </div>
          <ul className="flex flex-col gap-2">
            {order.items.map((item) => (
              <li key={item.id} className="flex flex-wrap justify-between gap-3 rounded-card bg-tile p-4 text-[16px]">
                <span>
                  <span className="block font-medium">{item.productName}</span>
                  <span className="text-[15px] text-muted">
                    {item.size} / {item.color} · Qty {item.quantity}
                  </span>
                </span>
                <span className="font-medium">{formatPrice(item.unitPriceCents * item.quantity)}</span>
              </li>
            ))}
          </ul>
          <div className="grid gap-4 tablet:grid-cols-2">
            <section className="rounded-card bg-tile p-5 text-[16px] leading-[24px]">
              <h3 className="mb-2 text-[18px] font-medium">Delivery</h3>
              <p>{order.recipient}</p>
              <p>{order.line1}</p>
              {order.line2 && <p>{order.line2}</p>}
              <p>
                {order.city}
                {order.region ? `, ${order.region}` : ""} {order.postalCode}
              </p>
              <p>{order.country}</p>
              <p className="mt-2 text-muted">{order.shippingMethod}</p>
            </section>
            <section className="rounded-card bg-tile p-5 text-[16px] leading-[28px]">
              <h3 className="mb-2 text-[18px] font-medium">Total</h3>
              <p className="flex justify-between">
                <span>Subtotal</span> {formatPrice(order.subtotalCents)}
              </p>
              {order.discountCents > 0 && (
                <p className="flex justify-between">
                  <span>Discount{order.promotionCode ? ` (${order.promotionCode})` : ""}</span> -{formatPrice(order.discountCents)}
                </p>
              )}
              <p className="flex justify-between">
                <span>Delivery</span> {formatPrice(order.shippingCents)}
              </p>
              <p className="mt-1 flex justify-between border-t border-faint/40 pt-1 font-medium">
                <span>Total</span> {formatPrice(order.totalCents)}
              </p>
            </section>
          </div>
          <OrderActions order={order} onChanged={reload} />
        </>
      )}
    </div>
  );
}
