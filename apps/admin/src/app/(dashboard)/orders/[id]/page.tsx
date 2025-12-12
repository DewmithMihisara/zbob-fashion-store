"use client";

import type { OrderDetail, OrderItem, Page } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use, useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, Table } from "@/components/ui/Table";
import { AddressFields, toAddressJson, type AddressValues } from "@/features/orders/AddressFields";
import { nextStatuses, statusLabels, statusTones } from "@/features/orders/status";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

function AddressForm({ order, onDone }: { order: OrderDetail; onDone: () => void }) {
  const [address, setAddress] = useState<AddressValues>({
    recipient: order.recipient,
    line1: order.line1,
    line2: order.line2 ?? "",
    city: order.city,
    region: order.region ?? "",
    postalCode: order.postalCode,
    country: order.country,
    phone: order.phone ?? "",
  });
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => api(`/admin/orders/${order.id}/address`, { method: "PUT", json: toAddressJson(address) }))) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <AddressFields value={address} onChange={setAddress} errors={fieldErrors} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function OrderPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const session = useSession();
  const { data: order, error, reload } = useData<OrderDetail>(`/admin/orders/${id}`);
  const [itemPage, setItemPage] = useState(0);
  const { data: items } = useData<Page<OrderItem>>(`/admin/orders/${id}/items?page=${itemPage}`);
  const [editingAddress, setEditingAddress] = useState(false);
  const action = useAction();

  async function remove() {
    if (await action.run(() => api(`/admin/orders/${id}`, { method: "DELETE" }))) {
      router.push("/orders");
    }
  }

  const canEditAddress = order?.status === "PENDING_PAYMENT" || order?.status === "PAID";

  return (
    <div className="max-w-[860px]">
      <Link href="/orders" className="text-[13px] text-muted hover:underline">
        Back to orders
      </Link>
      <PageHeader
        title={order?.orderNumber ?? "Order"}
        action={
          order && (
            <span className="inline-flex items-center gap-2">
              <StatusBadge tone={statusTones[order.status]}>{statusLabels[order.status]}</StatusBadge>
              {session?.role === "ADMIN" && order.status === "CANCELLED" && <ConfirmButton onConfirm={remove}>Delete order</ConfirmButton>}
            </span>
          )
        }
      />
      <ErrorAlert message={error ?? action.error} />
      {order && (
        <div className="flex flex-col gap-5">
          <div>
            <Table headers={["Item", "SKU", "Options", "Price", "Qty"]}>
              {items?.content.map((item) => (
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
            <Pagination page={items} onChange={setItemPage} />
          </div>
          <div className="grid gap-5 sm:grid-cols-2">
            <section className="rounded-xl border border-line bg-paper p-5 text-[13px] leading-6">
              <div className="mb-2 flex items-center justify-between">
                <h2 className="text-[15px] font-semibold">Deliver to</h2>
                {canEditAddress && (
                  <Button variant="secondary" onClick={() => setEditingAddress(true)}>
                    Edit address
                  </Button>
                )}
              </div>
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
      {editingAddress && order && (
        <Modal title="Edit delivery address" onClose={() => setEditingAddress(false)}>
          <AddressForm
            order={order}
            onDone={() => {
              setEditingAddress(false);
              reload();
            }}
          />
        </Modal>
      )}
    </div>
  );
}
