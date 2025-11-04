"use client";

import type { Category, Page, ProductSummary } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useData } from "@/hooks/useData";

const inputClass = "h-9 rounded-lg border border-line bg-paper px-3 text-[13px] outline-none focus:border-ink";

export default function ProductsPage() {
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [page, setPage] = useState(0);

  const query = new URLSearchParams({ page: String(page) });
  if (search.trim()) query.set("search", search.trim());
  if (category) query.set("category", category);

  const { data, error, loading } = useData<Page<ProductSummary>>(`/admin/products?${query}`);
  const { data: categories } = useData<Category[]>("/categories");

  return (
    <>
      <PageHeader
        title="Products"
        description="Everything in the store. Open a product to change its details, photos and stock."
        action={
          <Link href="/products/new" className="inline-flex h-9 items-center rounded-lg bg-ink px-3 text-[13px] font-medium text-paper hover:bg-neutral-800">
            Add product
          </Link>
        }
      />
      <div className="mb-4 flex flex-wrap gap-2">
        <input
          type="search"
          aria-label="Search products"
          placeholder="Search by name"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
          className={`${inputClass} w-[240px]`}
        />
        <select
          aria-label="Filter by category"
          value={category}
          onChange={(e) => {
            setCategory(e.target.value);
            setPage(0);
          }}
          className={inputClass}
        >
          <option value="">All categories</option>
          {categories?.map((c) => (
            <option key={c.id} value={c.slug}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <ErrorAlert message={error} />
      <Table headers={["Photo", "Name", "Price", "Status", "Stock"]}>
        {data?.content.map((product) => (
          <tr key={product.id} className="hover:bg-canvas">
            <Cell>
              <div className="size-12 overflow-hidden rounded-lg bg-tile">
                {product.imageUrl ? (
                  <img src={product.imageUrl} alt="" className="size-full object-cover" />
                ) : (
                  <span className="flex size-full items-center justify-center text-[10px] text-faint">No photo</span>
                )}
              </div>
            </Cell>
            <Cell>
              <Link href={`/products/${product.id}`} className="font-medium hover:underline">
                {product.name}
              </Link>
            </Cell>
            <Cell>{formatPrice(product.priceCents)}</Cell>
            <Cell>
              <StatusBadge tone={product.published ? "good" : "neutral"}>{product.published ? "Published" : "Draft"}</StatusBadge>
            </Cell>
            <Cell>
              <StatusBadge tone={product.inStock ? "good" : "bad"}>{product.inStock ? "In stock" : "Out of stock"}</StatusBadge>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No products found.</EmptyRow>}
        {!data && loading && <EmptyRow columns={5}>Loading...</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
