"use client";

import { useRouter } from "next/navigation";
import type { ShopOrder } from "@/lib/api";
import { SHOP_ORDERS, shopHref, type ShopQuery } from "@/lib/shop-query";

export function SortSelect({ query }: { query: ShopQuery }) {
  const router = useRouter();
  return (
    <label className="flex items-center gap-2 text-[16px] font-medium">
      <span className="text-muted">Sort by</span>
      <select
        value={query.order}
        onChange={(e) => router.push(shopHref(query, { order: e.target.value as ShopOrder, page: 1 }), { scroll: false })}
        className="rounded-full bg-paper px-[14px] py-[10px] text-[16px] outline-none focus:outline-2 focus:outline-ink"
      >
        {SHOP_ORDERS.map((order) => (
          <option key={order.value} value={order.value}>
            {order.label}
          </option>
        ))}
      </select>
    </label>
  );
}
