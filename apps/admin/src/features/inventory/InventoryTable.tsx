"use client";

import type { InventoryRow, Page } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { CheckboxField } from "@/components/ui/Field";
import { Pagination } from "@/components/ui/Pagination";
import { StockEditor } from "@/components/ui/StockEditor";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { StockStatus } from "@/features/products/ProductVariants";
import { useData } from "@/hooks/useData";

export function InventoryTable() {
  const router = useRouter();
  const lowOnly = useSearchParams().get("low") === "1";
  const [page, setPage] = useState(0);
  const [saveError, setSaveError] = useState<string | null>(null);
  const { data, error, loading, reload } = useData<Page<InventoryRow>>(`/admin/inventory?lowStockOnly=${lowOnly}&page=${page}`);

  return (
    <>
      <div className="mb-4">
        <CheckboxField
          label="Show only low and out of stock"
          checked={lowOnly}
          onChange={(e) => {
            setPage(0);
            router.replace(e.target.checked ? "/inventory?low=1" : "/inventory");
          }}
        />
      </div>
      <ErrorAlert message={error ?? saveError} />
      <Table headers={["Product", "SKU", "Size", "Colour", "In stock", "Status"]}>
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
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={6}>{lowOnly ? "Nothing is low on stock." : "No stock yet. Add sizes on a product."}</EmptyRow>}
        {!data && loading && <EmptyRow columns={6}>Loading...</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
