import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";

test("robots keeps private pages out and points to the sitemap", async ({ request }) => {
  const text = await (await request.get(`${STORE}/robots.txt`)).text();
  expect(text).toContain("Disallow: /account");
  expect(text).toContain("Disallow: /cart");
  expect(text).toMatch(/Sitemap: .+\/sitemap\.xml/);
});

test("the sitemap lists products and posts", async ({ request }) => {
  const response = await request.get(`${STORE}/sitemap.xml`);
  expect(response.status()).toBe(200);
  const xml = await response.text();
  expect(xml).toContain("/shop/field-pocket-cargo</loc>");
  expect(xml).toContain("/blog/why-we-design-in-small-batches</loc>");
});

test("product pages carry price, availability and preview tags", async ({ page }) => {
  await page.goto(`${STORE}/shop/field-pocket-cargo`);

  const data = JSON.parse((await page.locator('script[type="application/ld+json"]').first().textContent())!);
  expect(data["@type"]).toBe("Product");
  expect(data.offers.price).toBe("92.00");
  expect(data.offers.availability).toBe("https://schema.org/InStock");
  await expect(page.locator('meta[property="og:title"]')).toHaveAttribute("content", "Field Pocket Cargo");
  await expect(page.locator('link[rel="canonical"]')).toHaveAttribute("href", /\/shop\/field-pocket-cargo$/);
});

test("private pages ask search engines not to index them", async ({ page }) => {
  await page.goto(`${STORE}/login`);
  await expect(page.locator('meta[name="robots"]')).toHaveAttribute("content", /noindex/);
});
