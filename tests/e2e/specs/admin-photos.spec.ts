import { expect, test } from "@playwright/test";
import { ADMIN, errorAlert, openProduct, PNG, signIn, STORE } from "./helpers";

const PRODUCT = "Canvas Chore Jacket";
const SLUG = "canvas-chore-jacket";

test("uploading a product photo replaces the placeholder, and deleting brings it back", async ({ page }) => {
  await page.goto(`${STORE}/shop/${SLUG}`);
  await expect(page.getByRole("img", { name: /photo coming soon/ }).first()).toBeVisible();

  await signIn(page);
  await openProduct(page, PRODUCT);
  await page.getByLabel("Choose image file").setInputFiles({ name: "jacket.png", mimeType: "image/png", buffer: PNG });
  await expect(page.getByRole("img", { name: "Photo 1" })).toBeVisible();

  await page.goto(`${STORE}/shop/${SLUG}`);
  await expect(page.locator(`img[alt="${PRODUCT}"]`)).toBeVisible();
  await expect(page.getByRole("img", { name: new RegExp(`${PRODUCT} \\(photo coming soon\\)`) })).toHaveCount(0);

  await page.goto(`${STORE}/shop?search=Canvas`);
  await expect(page.locator(`article img[alt="${PRODUCT}"]`)).toBeVisible();

  await signIn(page);
  await openProduct(page, PRODUCT);
  await page.getByRole("button", { name: "Delete", exact: true }).first().click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();
  await expect(page.getByText("No photos yet.")).toBeVisible();

  await page.goto(`${STORE}/shop/${SLUG}`);
  await expect(page.getByRole("img", { name: new RegExp(`${PRODUCT} \\(photo coming soon\\)`) })).toBeVisible();
});

test("a file that is not an image is refused", async ({ page }) => {
  await signIn(page);
  await openProduct(page, PRODUCT);

  await page.getByLabel("Choose image file").setInputFiles({ name: "notes.txt", mimeType: "text/plain", buffer: Buffer.from("hello") });

  await expect(errorAlert(page)).toContainText("only JPEG, PNG and WebP");
});

test("a renamed non-image is refused by the api", async ({ page }) => {
  await signIn(page);
  await openProduct(page, PRODUCT);

  await page.getByLabel("Choose image file").setInputFiles({ name: "fake.png", mimeType: "image/png", buffer: Buffer.from("<script>alert(1)</script>") });

  await expect(errorAlert(page)).toContainText("Only JPEG, PNG and WebP images are allowed");
});

test("site photo slots update the storefront", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/site-photos`);
  const slot = page.getByRole("listitem").filter({ hasText: "Shop page hero" });

  await slot.getByLabel("Choose image file").setInputFiles({ name: "hero.png", mimeType: "image/png", buffer: PNG });
  await expect(slot.getByRole("img", { name: "Shop page hero" })).toBeVisible();

  await page.goto(`${STORE}/shop`);
  await expect(page.locator("section").first().locator("img")).toBeVisible();

  await page.goto(`${ADMIN}/site-photos`);
  await page.getByRole("listitem").filter({ hasText: "Shop page hero" }).getByRole("button", { name: "Remove" }).click();
  await page.getByRole("button", { name: "Yes", exact: true }).click();
  await expect(page.getByRole("listitem").filter({ hasText: "Shop page hero" }).getByText("Placeholder showing")).toBeVisible();
});
