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
import { VariantForm } from "./VariantForm";

export function InventoryTable() {
  const router = useRouter();
  const lowOnly = useSearchParams().get("low") === "1";
  const [page, setPage] = useState(0);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [editing, setEditing] = useState<InventoryRow | "new" | null>(null);
  const remove = useAction();
  const { data, error, loading, reload } = useData<Page<InventoryRow>>(`/admin/inventory?lowStockOnly=${lowOnly}&page=${page}`);

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
      <Table headers={["Product", "SKU", "Size", "Colour", "In stock", "Status", ""]}>
        {data?.content.map((row) => (
          <tr key={row.variantId}>
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
        {data?.content.length === 0 && <EmptyRow columns={7}>{lowOnly ? "Nothing is low on stock." : "No stock yet."}</EmptyRow>}
        {!data && loading && <EmptyRow columns={7}>Loading...</EmptyRow>}
      </Table>
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
