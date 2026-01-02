import type { ShopOrder } from "./api";

export const SHOP_ORDERS: { value: ShopOrder; label: string }[] = [
  { value: "newest", label: "Newest" },
  { value: "price-asc", label: "Price: low to high" },
  { value: "price-desc", label: "Price: high to low" },
  { value: "name", label: "Name" },
];

export interface ShopQuery {
  category?: string;
  search?: string;
  order: ShopOrder;
  page: number;
}

/** Reads the address bar safely: unknown sort orders and bad page numbers fall back to defaults. */
export function parseShopQuery(params: Record<string, string | string[] | undefined>): ShopQuery {
  const one = (key: string) => {
    const value = params[key];
    return (Array.isArray(value) ? value[0] : value)?.trim() || undefined;
  };
  const order = SHOP_ORDERS.some((o) => o.value === one("order")) ? (one("order") as ShopOrder) : "newest";
  const page = Number.parseInt(one("page") ?? "1", 10);
  return {
    category: one("category"),
    search: one("search")?.slice(0, 100),
    order,
    page: Number.isFinite(page) && page >= 1 ? page : 1,
  };
}

/** Builds a shop link that keeps the current filters, leaving out anything at its default. */
export function shopHref(query: ShopQuery, change: Partial<ShopQuery> = {}): string {
  const next = { ...query, ...change };
  const params = new URLSearchParams();
  if (next.category) params.set("category", next.category);
  if (next.search) params.set("search", next.search);
  if (next.order !== "newest") params.set("order", next.order);
  if (next.page > 1) params.set("page", String(next.page));
  const text = params.toString();
  return text ? `/shop?${text}` : "/shop";
}
