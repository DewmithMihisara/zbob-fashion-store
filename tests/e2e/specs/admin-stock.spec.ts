import { expect, test } from "@playwright/test";
import { ADMIN, API, openProduct, signIn, STORE } from "./helpers";

const PRODUCT = "Loop Back Short";
const SLUG = "loop-back-short";

async function setAllStock(page: import("@playwright/test").Page, quantity: string) {
  await openProduct(page, PRODUCT);
  for (const size of ["S", "M", "L"]) {
    const input = page.getByLabel(`Stock for ${size} Black`);
    await input.fill(quantity);
    const row = page.getByRole("row").filter({ has: input });
    const save = row.getByRole("button", { name: "Save" });
    if (await save.count()) {
      await save.click();
      await expect(save).toHaveCount(0);
    }
  }
}

test("setting all stock to 0 shows out of stock, and restoring it brings Order Now back", async ({ page }) => {
  await signIn(page);
  await setAllStock(page, "0");

  await page.goto(`${STORE}/shop/${SLUG}`);
  const soldOut = page.getByRole("button", { name: "Out of stock" });
  await expect(soldOut).toBeDisabled();
  await page.goto(`${STORE}/shop`);
  await expect(page.locator("article").filter({ hasText: PRODUCT }).getByText("Out of stock")).toBeVisible();

  await signIn(page);
  await setAllStock(page, "12");

  await page.goto(`${STORE}/shop/${SLUG}`);
  await expect(page.getByRole("button", { name: "Order Now" })).toBeEnabled();
});

test("stock page edits, filters low stock and flags it on the dashboard", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory?low=1`);
  await expect(page.getByRole("checkbox", { name: "Show only low and out of stock" })).toBeChecked();
  const before = await page.getByRole("row").count();

  await page.goto(`${ADMIN}/inventory`);
  const row = page.getByRole("row").filter({ hasText: "Harbour Camp Shirt" }).first();
  await row.getByRole("spinbutton").fill("1");
  await row.getByRole("button", { name: "Save" }).click();
  await expect(row.getByText("Low stock")).toBeVisible();

  await page.goto(`${ADMIN}/inventory?low=1`);
  await expect(page.getByRole("row").filter({ hasText: "Harbour Camp Shirt" }).first()).toBeVisible();
  expect(await page.getByRole("row").count()).toBeGreaterThan(before);
});

test("the public api never reveals stock counts", async ({ request }) => {
  const response = await request.get(`${API}/products/${SLUG}`);
  const body = await response.json();

  expect(body).toHaveProperty("inStock");
  expect(JSON.stringify(body)).not.toContain("stockQuantity");
});
