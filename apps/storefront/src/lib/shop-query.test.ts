import { describe, expect, it } from "vitest";
import { parseShopQuery, shopHref } from "./shop-query";

describe("parseShopQuery", () => {
  it("reads filters, sort and page", () => {
    expect(parseShopQuery({ category: "mens-wear", search: " coat ", order: "price-asc", page: "3" })).toEqual({
      category: "mens-wear",
      search: "coat",
      order: "price-asc",
      page: 3,
    });
  });

  it("falls back for unknown or broken values", () => {
    expect(parseShopQuery({ order: "createdAt,desc", page: "-2" })).toEqual({ category: undefined, search: undefined, order: "newest", page: 1 });
    expect(parseShopQuery({ page: "abc" }).page).toBe(1);
  });

  it("uses the first value when a key repeats", () => {
    expect(parseShopQuery({ search: ["tee", "coat"] }).search).toBe("tee");
  });
});

describe("shopHref", () => {
  const base = { category: "mens-wear", search: "coat", order: "price-desc" as const, page: 2 };

  it("keeps the other filters when one changes", () => {
    expect(shopHref(base, { page: 3 })).toBe("/shop?category=mens-wear&search=coat&order=price-desc&page=3");
  });

  it("drops values at their defaults", () => {
    expect(shopHref({ order: "newest", page: 1 })).toBe("/shop");
    expect(shopHref(base, { search: undefined, order: "newest", page: 1 })).toBe("/shop?category=mens-wear");
  });
});
