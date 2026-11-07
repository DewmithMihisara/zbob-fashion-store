import type { PublicProduct } from "@zbob/shared-types";
import { describe, expect, it } from "vitest";
import { productJsonLd, serializeJsonLd } from "./structured-data";

const product: PublicProduct = {
  id: 1,
  name: "Boxy Tee",
  slug: "boxy-tee",
  description: "Soft",
  material: null,
  care: null,
  warranty: null,
  badge: null,
  priceCents: 3550,
  compareAtPriceCents: null,
  inStock: false,
  category: { id: 1, name: "Men's Wear", slug: "mens-wear", description: null, badge: null, position: 0, sizeGuide: null },
  images: [{ id: 1, url: "https://cdn.example/a.png", altText: null, position: 0 }],
  options: [],
};

describe("productJsonLd", () => {
  it("describes price and availability", () => {
    const data = productJsonLd(product, null) as { offers: { price: string; availability: string }; image: string[] };
    expect(data.offers.price).toBe("35.50");
    expect(data.offers.availability).toBe("https://schema.org/OutOfStock");
    expect(data.image).toEqual(["https://cdn.example/a.png"]);
  });

  it("adds the rating only when there are reviews", () => {
    expect(productJsonLd(product, { averageRating: 0, reviewCount: 0, reviews: { content: [], page: 0, size: 1, totalElements: 0, totalPages: 0 } })).not.toHaveProperty("aggregateRating");
    expect(productJsonLd(product, { averageRating: 4.5, reviewCount: 2, reviews: { content: [], page: 0, size: 1, totalElements: 2, totalPages: 2 } })).toHaveProperty("aggregateRating.ratingValue", 4.5);
  });
});

describe("serializeJsonLd", () => {
  it("can not be used to break out of the script tag", () => {
    const text = serializeJsonLd({ name: "</script><script>alert(1)</script>" });
    expect(text).not.toContain("</script>");
    expect(JSON.parse(text).name).toBe("</script><script>alert(1)</script>");
  });
});
