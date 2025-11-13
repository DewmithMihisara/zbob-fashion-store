"use client";

import type { ProductVariant } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { TextField } from "@/components/ui/Field";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { StockEditor } from "@/components/ui/StockEditor";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { api, errorMessage } from "@/lib/api";

export function ProductVariants({ productId, variants, onChange }: { productId: number; variants: ProductVariant[]; onChange: () => void }) {
  const [sku, setSku] = useState("");
  const [size, setSize] = useState("");
  const [color, setColor] = useState("");
  const [stock, setStock] = useState("0");
  const [threshold, setThreshold] = useState("5");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function add(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api(`/admin/products/${productId}/variants`, {
        method: "POST",
        json: { sku, size, color, stockQuantity: Number(stock), lowStockThreshold: Number(threshold) },
      });
      setSku("");
      setSize("");
      setColor("");
      setStock("0");
      onChange();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  async function remove(id: number) {
    setError(null);
    try {
      await api(`/admin/variants/${id}`, { method: "DELETE" });
      onChange();
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  return (
    <section className="rounded-xl border border-line bg-paper p-5">
      <h2 className="text-[16px] font-semibold">Stock</h2>
      <p className="mb-3 text-[13px] text-muted">Each size and colour has its own count. The website shows out of stock when every count is 0.</p>
      <ErrorAlert message={error} />
      <div className="mt-2">
        <Table headers={["SKU", "Size", "Colour", "In stock", "Status", ""]}>
          {variants.map((variant) => (
            <tr key={variant.id}>
              <Cell>{variant.sku}</Cell>
              <Cell>{variant.size}</Cell>
              <Cell>{variant.color}</Cell>
              <Cell>
                <StockEditor
                  key={variant.stockQuantity}
                  variantId={variant.id}
                  quantity={variant.stockQuantity}
                  label={`${variant.size} ${variant.color}`}
                  onSaved={onChange}
                  onError={setError}
                />
              </Cell>
              <Cell>
                <StockStatus quantity={variant.stockQuantity} low={variant.lowStock} />
              </Cell>
              <Cell className="text-right">
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove(variant.id)}>
                  Delete
                </ConfirmButton>
              </Cell>
            </tr>
          ))}
          {variants.length === 0 && <EmptyRow columns={6}>Add a size below so this product can show as in stock.</EmptyRow>}
        </Table>
      </div>
      <form onSubmit={add} className="mt-4 grid items-start gap-3 sm:grid-cols-[1.4fr_0.7fr_1fr_0.7fr_0.7fr_auto]">
        <TextField label="SKU" required maxLength={64} value={sku} onChange={(e) => setSku(e.target.value)} />
        <TextField label="Size" required maxLength={20} value={size} onChange={(e) => setSize(e.target.value)} />
        <TextField label="Colour" required maxLength={40} value={color} onChange={(e) => setColor(e.target.value)} />
        <TextField label="Stock" required type="number" min={0} value={stock} onChange={(e) => setStock(e.target.value)} />
        <TextField label="Low at" required type="number" min={0} value={threshold} onChange={(e) => setThreshold(e.target.value)} />
        <Button type="submit" disabled={busy} className="mt-[22px]">
          Add
        </Button>
      </form>
    </section>
  );
}

export function StockStatus({ quantity, low }: { quantity: number; low: boolean }) {
  if (quantity === 0) {
    return <StatusBadge tone="bad">Out of stock</StatusBadge>;
  }
  return low ? <StatusBadge tone="warn">Low stock</StatusBadge> : <StatusBadge tone="good">In stock</StatusBadge>;
}
