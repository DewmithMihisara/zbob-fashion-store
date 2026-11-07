"use client";

import type { Page, ProductVariant } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { Modal } from "@/components/ui/Modal";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { StockEditor } from "@/components/ui/StockEditor";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { VariantForm } from "@/features/inventory/VariantForm";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export function ProductVariants({ productId, onChange }: { productId: number; onChange: () => void }) {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<ProductVariant>>(`/admin/products/${productId}/variants?page=${page}`);
  const [editing, setEditing] = useState<ProductVariant | "new" | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const remove = useAction();

  function refresh() {
    reload();
    onChange();
  }

  return (
    <section className="rounded-xl border border-line bg-paper p-5">
      <div className="mb-3 flex flex-wrap items-start justify-between gap-2">
        <div>
          <h2 className="text-[16px] font-semibold">Stock</h2>
          <p className="text-[13px] text-muted">Each size and colour has its own count. The website shows out of stock when every count is 0.</p>
        </div>
        <Button onClick={() => setEditing("new")}>Add size</Button>
      </div>
      <ErrorAlert message={error ?? saveError ?? remove.error} />
      <div className="mt-2">
        <Table headers={["SKU", "Size", "Colour", "In stock", "Status", ""]}>
          {data?.content.map((variant) => (
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
                  onSaved={refresh}
                  onError={setSaveError}
                />
              </Cell>
              <Cell>
                <StockStatus quantity={variant.stockQuantity} low={variant.lowStock} />
              </Cell>
              <Cell className="text-right">
                <span className="inline-flex gap-1">
                  <Button variant="secondary" onClick={() => setEditing(variant)}>
                    Edit
                  </Button>
                  <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/variants/${variant.id}`, { method: "DELETE" })).then(refresh)}>
                    Delete
                  </ConfirmButton>
                </span>
              </Cell>
            </tr>
          ))}
          {data?.content.length === 0 && <EmptyRow columns={6}>Add a size so this product can show as in stock.</EmptyRow>}
        </Table>
      </div>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add size" : `Edit ${editing.sku}`} onClose={() => setEditing(null)}>
          <VariantForm
            productId={productId}
            variant={editing === "new" ? undefined : editing}
            onDone={() => {
              setEditing(null);
              refresh();
            }}
          />
        </Modal>
      )}
    </section>
  );
}

export function StockStatus({ quantity, low }: { quantity: number; low: boolean }) {
  if (quantity === 0) {
    return <StatusBadge tone="bad">Out of stock</StatusBadge>;
  }
  return low ? <StatusBadge tone="warn">Low stock</StatusBadge> : <StatusBadge tone="good">In stock</StatusBadge>;
}
