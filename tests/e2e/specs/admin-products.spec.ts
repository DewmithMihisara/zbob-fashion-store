import { expect, test } from "@playwright/test";
import { ADMIN, signIn, STORE } from "./helpers";

test("a new draft stays hidden until published", async ({ page }) => {
  const name = `E2E Tee ${Date.now()}`;
  const slug = name.toLowerCase().replaceAll(" ", "-");

  await signIn(page);
  await page.goto(`${ADMIN}/products/new`);
  await page.getByLabel("Name").fill(name);
  await page.getByLabel("Description").fill("Made by an automated test.");
  await page.getByLabel("Price", { exact: true }).fill("20");
  await page.getByLabel("Category").selectOption({ label: "Men's Wear" });
  await page.getByRole("button", { name: "Create product" }).click();
  await expect(page.getByRole("heading", { name })).toBeVisible();

  const response = await page.goto(`${STORE}/shop/${slug}`);
  expect(response?.status()).toBe(404);

  await signIn(page);
  await page.goto(`${ADMIN}/products`);
  await page.getByLabel("Search products").fill(name);
  await page.getByRole("link", { name }).click();
  await page.getByLabel("Published (visible on the website)").check();
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByRole("status")).toHaveText("Saved.");

  await page.goto(`${STORE}/shop/${slug}`);
  await expect(page.getByRole("heading", { level: 1 })).toHaveText(name);
  await expect(page.getByRole("button", { name: "Out of stock" })).toBeVisible();
});

test("an invalid price is explained", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/products/new`);
  await page.getByLabel("Name").fill("Bad Price");
  await page.getByLabel("Description").fill("Text");
  await page.getByLabel("Price", { exact: true }).fill("abc");
  await page.getByLabel("Category").selectOption({ label: "Men's Wear" });
  await page.getByRole("button", { name: "Create product" }).click();

  await expect(page.getByText("Enter a price like 35.00")).toBeVisible();
});
