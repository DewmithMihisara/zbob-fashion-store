"use client";

import type { Page, ProductSummary } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { SelectField, TextField } from "@/components/ui/Field";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export interface EditableVariant {
  id: number;
  sku: string;
  size: string;
  color: string;
  stockQuantity: number;
  lowStockThreshold: number;
}

/** The product picker only appears when adding a row from the stock page. */
function ProductPicker({ value, onChange }: { value: string; onChange: (id: string) => void }) {
  // 100 is the most the API returns at once; beyond that, add the row from the product's own page.
  const { data } = useData<Page<ProductSummary>>("/admin/products?size=100&sort=name");
  return (
    <SelectField label="Product" required value={value} onChange={(e) => onChange(e.target.value)}>
      <option value="">Choose...</option>
      {data?.content.map((product) => (
        <option key={product.id} value={product.id}>
          {product.name}
        </option>
      ))}
    </SelectField>
  );
}

export function VariantForm({
  variant,
  productId,
  onDone,
}: {
  variant?: EditableVariant;
  productId?: number;
  onDone: () => void;
}) {
  const [chosenProduct, setChosenProduct] = useState("");
  const [sku, setSku] = useState(variant?.sku ?? "");
  const [size, setSize] = useState(variant?.size ?? "");
  const [color, setColor] = useState(variant?.color ?? "");
  const [stock, setStock] = useState(String(variant?.stockQuantity ?? 0));
  const [threshold, setThreshold] = useState(String(variant?.lowStockThreshold ?? 5));
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const json = { sku, size, color, stockQuantity: Number(stock), lowStockThreshold: Number(threshold) };
    const ok = await run(() =>
      variant
        ? api(`/admin/variants/${variant.id}`, { method: "PUT", json })
        : api(`/admin/products/${productId ?? chosenProduct}/variants`, { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      {!variant && productId === undefined && <ProductPicker value={chosenProduct} onChange={setChosenProduct} />}
      <TextField label="SKU" required maxLength={64} value={sku} onChange={(e) => setSku(e.target.value)} error={fieldErrors.sku} />
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField label="Size" required maxLength={20} value={size} onChange={(e) => setSize(e.target.value)} error={fieldErrors.size} />
        <TextField label="Colour" required maxLength={40} value={color} onChange={(e) => setColor(e.target.value)} error={fieldErrors.color} />
        <TextField label="In stock" required type="number" min={0} value={stock} onChange={(e) => setStock(e.target.value)} error={fieldErrors.stockQuantity} />
        <TextField label="Low stock at" required type="number" min={0} value={threshold} onChange={(e) => setThreshold(e.target.value)} error={fieldErrors.lowStockThreshold} />
      </div>
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}
