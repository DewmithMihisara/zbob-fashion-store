"use client";

import type { Page, ProductSummary } from "@zbob/shared-types";
import { useState } from "react";
import { ProductCard } from "@/components/product/ProductCard";
import { buttonClass, LinkButton } from "@/components/ui/Button";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerData } from "@/hooks/useCustomerData";

export default function WishlistPage() {
  const [page, setPage] = useState(0);
  const { data, error } = useCustomerData<Page<ProductSummary>>(`/me/wishlist?page=${page}`);

  return (
    <div className="flex flex-col gap-4">
      <ErrorNotice message={error} />
      {data?.content.length === 0 && page === 0 && (
        <div className="flex flex-col items-center gap-4 py-10 text-center">
          <p className="text-[18px] text-muted">Nothing saved yet. Tap the heart on any product to keep it here.</p>
          <LinkButton href="/shop">Shop all items</LinkButton>
        </div>
      )}
      <div className="grid grid-cols-1 gap-x-[15px] gap-y-8 tablet:grid-cols-2 desktop:grid-cols-3">
        {data?.content.map((product) => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
      {data && (data.totalPages > 1 || page > 0) && (
        <nav aria-label="Wishlist pages" className="mt-2 flex justify-center gap-2">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)} className={buttonClass("light", "disabled:opacity-40")}>
            Previous
          </button>
          <button type="button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)} className={buttonClass("light", "disabled:opacity-40")}>
            Next
          </button>
        </nav>
      )}
    </div>
  );
}
