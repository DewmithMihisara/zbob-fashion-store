import { expect, test } from "@playwright/test";
import { ADMIN, signIn, STORE } from "./helpers";

test("a size guide written in the dashboard shows as a table on product pages", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/categories`);
  await page.getByRole("row").filter({ hasText: "Women's Wear" }).getByRole("button", { name: "Edit" }).click();
  await page.getByLabel(/^Size guide/).fill("Measured flat, in centimetres.\nSize | Bust | Length\nS | 84 | 96\nM | 90 | 98");
  await page.getByRole("dialog").getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);

  await page.goto(`${STORE}/shop/linen-weekend-dress`);
  await page.getByRole("button", { name: "Size guide" }).click();
  const guide = page.getByRole("dialog", { name: "Size guide" });
  await expect(guide.getByRole("columnheader", { name: "Bust" })).toBeVisible();
  await expect(guide.getByRole("row").filter({ hasText: "M" })).toContainText("90");
  await expect(guide.getByText("Measured flat, in centimetres.")).toBeVisible();
});

test("products in a category without a guide show no size guide button", async ({ page }) => {
  await page.goto(`${STORE}/shop/dungaree-play-set`);
  await expect(page.getByRole("button", { name: "Add to cart" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Size guide" })).toHaveCount(0);
});

test("a swatch colour set on a stock row shows beside the colour name", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory`);
  const row = page.getByRole("row").filter({ hasText: "Second Skin Jersey Tee" }).first();
  await row.getByRole("button", { name: "Edit" }).click();
  await page.getByLabel(/^Swatch colour/).fill("#1F2A44");
  await page.getByRole("dialog").getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);

  await page.goto(`${STORE}/shop/second-skin-jersey-tee`);
  const swatch = page.getByRole("group", { name: "Colour" }).locator('span[aria-hidden="true"]').first();
  await expect(swatch).toHaveCSS("background-color", "rgb(31, 42, 68)");
});
