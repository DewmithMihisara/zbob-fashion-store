import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";

function prices(texts: string[]): number[] {
  return texts.map((text) => Number(text.replace(/[^0-9.]/g, "")));
}

test("search finds products by name and says how many", async ({ page }) => {
  await page.goto(`${STORE}/shop`);
  await page.getByLabel("Search products").fill("coat");
  await page.getByRole("button", { name: "Search" }).click();

  await expect(page).toHaveURL(/search=coat/);
  await expect(page.getByTestId("result-count")).toHaveText(/products? for “coat”$/);
  for (const name of await page.locator("article h3").allInnerTexts()) {
    expect(name.toLowerCase()).toContain("coat");
  }
});

test("a search with no matches says so", async ({ page }) => {
  await page.goto(`${STORE}/shop?search=zzznothing`);
  await expect(page.getByTestId("result-count")).toHaveText("0 products for “zzznothing”");
  await expect(page.getByText("New pieces are on the way.")).toBeVisible();
});

test("sorting by price orders the cards and keeps the category", async ({ page }) => {
  await page.goto(`${STORE}/shop?category=childrens-wear`);
  await page.getByLabel("Sort by").selectOption("price-asc");

  await expect(page).toHaveURL(/category=childrens-wear.*order=price-asc|order=price-asc.*category=childrens-wear/);
  const shown = prices(await page.locator("article").evaluateAll((cards) => cards.map((c) => c.querySelector("span.font-medium")?.textContent ?? "")));
  expect(shown).toEqual([...shown].sort((a, b) => a - b));
});

test("page numbers move through the results", async ({ page }) => {
  await page.goto(`${STORE}/shop?order=name`);
  const firstPage = await page.locator("article h3").allInnerTexts();

  await page.getByRole("link", { name: "Page 2" }).click();

  await expect(page).toHaveURL(/page=2/);
  await expect(page.getByRole("link", { name: "Page 2" })).toHaveAttribute("aria-current", "page");
  const secondPage = await page.locator("article h3").allInnerTexts();
  expect(secondPage.some((name) => firstPage.includes(name))).toBe(false);
});

test("the header search opens the shop search box", async ({ page }) => {
  await page.goto(STORE);
  await page.getByRole("link", { name: "Search products" }).click();
  await expect(page).toHaveURL(/\/shop#search$/);
  await expect(page.getByLabel("Search products")).toBeVisible();
});
