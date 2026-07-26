"use client";

import type { InventoryRow, Page } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Pagination } from "@/components/ui/Pagination";
import { StockEditor } from "@/components/ui/StockEditor";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { StockStatus } from "@/features/products/ProductVariants";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";
import { BulkStockBar } from "./BulkStockBar";
import { VariantForm } from "./VariantForm";

export function InventoryTable() {
  const router = useRouter();
  const lowOnly = useSearchParams().get("low") === "1";
  const [page, setPage] = useState(0);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [editing, setEditing] = useState<InventoryRow | "new" | null>(null);
  const remove = useAction();
  const path = `/admin/inventory?lowStockOnly=${lowOnly}&page=${page}`;
  const { data, error, loading, reload } = useData<Page<InventoryRow>>(path);

  // A selection belongs to the page it was made on, so changing page or filter starts afresh.
  const [selection, setSelection] = useState<{ path: string; ids: Set<number> }>({ path, ids: new Set() });
  const selected = selection.path === path ? selection.ids : new Set<number>();
  const rows = data?.content ?? [];
  const allTicked = rows.length > 0 && rows.every((row) => selected.has(row.variantId));

  function toggle(id: number) {
    const next = new Set(selected);
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    setSelection({ path, ids: next });
  }

  function toggleAll() {
    setSelection({ path, ids: allTicked ? new Set() : new Set(rows.map((row) => row.variantId)) });
  }

  function clear() {
    setSelection({ path, ids: new Set() });
  }

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <CheckboxField
          label="Show only low and out of stock"
          checked={lowOnly}
          onChange={(e) => {
            setPage(0);
            router.replace(e.target.checked ? "/inventory?low=1" : "/inventory");
          }}
        />
        <Button onClick={() => setEditing("new")}>Add stock row</Button>
      </div>
      <ErrorAlert message={error ?? saveError ?? remove.error} />
      <Table headers={["", "Product", "SKU", "Size", "Colour", "In stock", "Status", ""]}>
        {rows.length > 0 && (
          <tr className="bg-canvas">
            <Cell>
              <input type="checkbox" aria-label="Tick every row on this page" checked={allTicked} onChange={toggleAll} className="size-4 accent-black" />
            </Cell>
            <Cell className="text-[12px] text-muted">Tick rows to change their stock together</Cell>
            <Cell />
            <Cell />
            <Cell />
            <Cell />
            <Cell />
            <Cell />
          </tr>
        )}
        {rows.map((row) => (
          <tr key={row.variantId} className={selected.has(row.variantId) ? "bg-orange-50/40" : undefined}>
            <Cell>
              <input
                type="checkbox"
                aria-label={`Tick ${row.productName} ${row.size} ${row.color}`}
                checked={selected.has(row.variantId)}
                onChange={() => toggle(row.variantId)}
                className="size-4 accent-black"
              />
            </Cell>
            <Cell>
              <Link href={`/products/${row.productId}`} className="font-medium hover:underline">
                {row.productName}
              </Link>
            </Cell>
            <Cell>{row.sku}</Cell>
            <Cell>{row.size}</Cell>
            <Cell>{row.color}</Cell>
            <Cell>
              <StockEditor
                key={row.stockQuantity}
                variantId={row.variantId}
                quantity={row.stockQuantity}
                label={`${row.productName} ${row.size} ${row.color}`}
                onSaved={reload}
                onError={setSaveError}
              />
            </Cell>
            <Cell>
              <StockStatus quantity={row.stockQuantity} low={row.lowStock} />
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(row)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/variants/${row.variantId}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={8}>{lowOnly ? "Nothing is low on stock." : "No stock yet."}</EmptyRow>}
        {!data && loading && <EmptyRow columns={8}>Loading...</EmptyRow>}
      </Table>
      {/* Below the table and stuck to the bottom of the screen, so it stays reachable on a long page. */}
      {selected.size > 0 && (
        <div className="sticky bottom-4 z-10 mt-4">
          <BulkStockBar
            variantIds={[...selected]}
            onClear={clear}
            onDone={() => {
              clear();
              reload();
            }}
          />
        </div>
      )}
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add stock row" : `Edit ${editing.sku}`} onClose={() => setEditing(null)}>
          <VariantForm
            variant={editing === "new" ? undefined : { ...editing, id: editing.variantId }}
            onDone={() => {
              setEditing(null);
              reload();
            }}
          />
        </Modal>
      )}
    </>
  );
}
