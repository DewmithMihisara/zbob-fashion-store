"use client";

import type { InventoryRow, OrderDetail, Page, ShippingMethod, UserAccount } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { SelectField, TextField } from "@/components/ui/Field";
import { PageHeader } from "@/components/ui/PageHeader";
import { AddressFields, emptyAddress, toAddressJson } from "@/features/orders/AddressFields";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

interface Line {
  key: number;
  variantId: string;
  quantity: string;
}

export default function NewOrderPage() {
  const router = useRouter();
  // The lists below are limited to 100 rows each, the most the API returns at once.
  const { data: customers } = useData<Page<UserAccount>>("/admin/customers?size=100&sort=fullName");
  const { data: stock } = useData<Page<InventoryRow>>("/admin/inventory?size=100");
  const { data: shipping } = useData<Page<ShippingMethod>>("/admin/shipping-methods?size=100");

  const [customerId, setCustomerId] = useState("");
  const [shippingMethodId, setShippingMethodId] = useState("");
  const [promotionCode, setPromotionCode] = useState("");
  const [lines, setLines] = useState<Line[]>([{ key: 0, variantId: "", quantity: "1" }]);
  const [address, setAddress] = useState(emptyAddress);
  const { run, busy, error, fieldErrors } = useAction();

  function updateLine(key: number, change: Partial<Line>) {
    setLines((current) => current.map((line) => (line.key === key ? { ...line, ...change } : line)));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const order = await api<OrderDetail>("/admin/orders", {
        method: "POST",
        json: {
          customerId: Number(customerId),
          shippingMethodId: Number(shippingMethodId),
          promotionCode: promotionCode || null,
          items: lines.map((line) => ({ variantId: Number(line.variantId), quantity: Number(line.quantity) })),
          address: toAddressJson(address),
        },
      });
      router.push(`/orders/${order.id}`);
    });
  }

  return (
    <div className="max-w-[760px]">
      <Link href="/orders" className="text-[13px] text-muted hover:underline">
        Back to orders
      </Link>
      <PageHeader title="Add order" description="For orders taken another way, such as by phone. Prices come from the products, and stock goes down when you save." />
      <form onSubmit={submit} className="flex flex-col gap-4 rounded-xl border border-line bg-paper p-5">
        <ErrorAlert message={error} />
        <SelectField label="Customer" required value={customerId} onChange={(e) => setCustomerId(e.target.value)} error={fieldErrors.customerId}>
          <option value="">Choose...</option>
          {customers?.content.map((customer) => (
            <option key={customer.id} value={customer.id}>
              {customer.fullName} ({customer.email})
            </option>
          ))}
        </SelectField>
        {lines.map((line, index) => (
          <div key={line.key} className="grid items-start gap-3 sm:grid-cols-[1fr_90px_auto]">
            <SelectField label={`Item ${index + 1}`} required value={line.variantId} onChange={(e) => updateLine(line.key, { variantId: e.target.value })}>
              <option value="">Choose...</option>
              {stock?.content.map((row) => (
                <option key={row.variantId} value={row.variantId} disabled={row.stockQuantity === 0}>
                  {row.productName}, {row.size}, {row.color} ({row.stockQuantity} in stock)
                </option>
              ))}
            </SelectField>
            <TextField label="Quantity" required type="number" min={1} max={99} value={line.quantity} onChange={(e) => updateLine(line.key, { quantity: e.target.value })} />
            <Button variant="secondary" className="mt-[22px]" disabled={lines.length === 1} onClick={() => setLines((current) => current.filter((other) => other.key !== line.key))}>
              Remove
            </Button>
          </div>
        ))}
        <div>
          <Button variant="secondary" onClick={() => setLines((current) => [...current, { key: Math.max(...current.map((l) => l.key)) + 1, variantId: "", quantity: "1" }])}>
            Add another item
          </Button>
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          <SelectField label="Delivery" required hint={shipping?.content.length === 0 ? "Add a delivery option on the Shipping page first." : undefined} value={shippingMethodId} onChange={(e) => setShippingMethodId(e.target.value)}>
            <option value="">Choose...</option>
            {shipping?.content.map((method) => (
              <option key={method.id} value={method.id}>
                {method.name}
              </option>
            ))}
          </SelectField>
          <TextField label="Promotion code" maxLength={40} value={promotionCode} onChange={(e) => setPromotionCode(e.target.value)} />
        </div>
        <AddressFields value={address} onChange={setAddress} errors={fieldErrors} />
        <div>
          <Button type="submit" disabled={busy}>
            {busy ? "Saving..." : "Create order"}
          </Button>
        </div>
      </form>
    </div>
  );
}
