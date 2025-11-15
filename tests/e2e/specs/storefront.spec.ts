import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";

test("home page shows every section with live data", async ({ page }) => {
  await page.goto(STORE);

  await expect(page.getByRole("heading", { level: 1 })).toContainText("Clothes that keep");
  for (const heading of ["our latest drop", "Made to be worn, often", "keep coming back for", "simple, lasting ideas", "In our customers' words", "Good style, less effort", "dressed every day", "wear zbob every day"]) {
    await expect(page.getByRole("heading", { name: heading })).toBeVisible();
  }
});

test("home page has no invented claims from the template", async ({ page }) => {
  await page.goto(STORE);
  const text = await page.locator("body").innerText();

  expect(text.toLowerCase()).not.toContain("wearix");
  expect(text).not.toContain("1k+");
  expect(text).not.toContain("Since 2014");
});

test("shop filters by category", async ({ page }) => {
  await page.goto(`${STORE}/shop`);
  await expect(page.locator("article")).toHaveCount(20);

  await page.getByRole("link", { name: "Children's Wear" }).click();

  await expect(page).toHaveURL(/category=childrens-wear/);
  await expect(page.locator("article")).toHaveCount(4);
  await expect(page.getByRole("link", { name: "Children's Wear" })).toHaveAttribute("aria-current", "page");
});

test("product card opens the product page", async ({ page }) => {
  await page.goto(`${STORE}/shop`);

  await page.getByRole("link", { name: "Everyday Square Tee" }).first().click();

  await expect(page).toHaveURL(/\/shop\/everyday-square-tee$/);
  await expect(page.getByRole("heading", { level: 1 })).toHaveText("Everyday Square Tee");
  await expect(page.getByText("USD $35.00", { exact: true })).toBeVisible();
});

test("products without photos show the placeholder", async ({ page }) => {
  await page.goto(`${STORE}/shop/ribbed-merino-crew`);

  await expect(page.getByRole("img", { name: /Ribbed Merino Crew \(photo coming soon\)/ })).toBeVisible();
});

test("unknown product shows the not found page", async ({ page }) => {
  const response = await page.goto(`${STORE}/shop/does-not-exist`);

  expect(response?.status()).toBe(404);
  await expect(page.getByRole("heading", { name: "Page not found" })).toBeVisible();
});

test("blog filters by topic and opens a post", async ({ page }) => {
  await page.goto(`${STORE}/blog`);
  await page.getByRole("link", { name: "Brand Stories" }).click();
  await expect(page).toHaveURL(/topic=Brand/);

  await page.getByRole("link", { name: "Why we design in small batches" }).first().click();

  await expect(page.getByRole("heading", { level: 1 })).toHaveText("Why we design in small batches");
  await expect(page.getByRole("heading", { name: "Start with what you already wear" })).toBeVisible();
});

test("about page figures come from the store", async ({ page }) => {
  await page.goto(`${STORE}/about`);

  await expect(page.getByText("Styles available")).toBeVisible();
  await expect(page.getByText("10M+")).toHaveCount(0);
});

test("mobile menu opens and navigates", async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(STORE);

  await page.getByRole("button", { name: "Open menu" }).click();
  await page.getByRole("navigation", { name: "Mobile" }).getByRole("link", { name: "About" }).click();

  await expect(page).toHaveURL(/\/about$/);
});
