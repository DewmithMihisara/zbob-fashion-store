import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";
import { asAdmin, newShippingMethod, registerInBrowser, uniqueEmail } from "./customer-helpers";

test("checkout works out the total with delivery and a promo code", async ({ page, request }) => {
  const shipping = await newShippingMethod(request, 500);
  const code = `SAVE${Date.now().toString(36).toUpperCase()}`;
  const admin = await asAdmin(request);
  await admin.post("/admin/promotions", { code, discountType: "PERCENT", discountValue: 10, minimumCents: 0, active: true });

  await registerInBrowser(page, uniqueEmail("checkout"));
  await page.goto(`${STORE}/shop/field-pocket-cargo`);
  await page.getByRole("button", { name: "Order Now" }).click();
  await expect(page.getByRole("heading", { name: "Checkout" })).toBeVisible();
  const summary = page.getByRole("complementary", { name: "Order summary" });
  await expect(summary.getByRole("list", { name: "Still needed" })).toContainText("Choose a delivery address.");

  await page.getByRole("link", { name: "Add an address" }).click();
  await page.getByRole("button", { name: "Add an address" }).click();
  await page.getByLabel("Recipient").fill("Nadia Perera");
  await page.getByLabel("Address line 1").fill("12 Galle Road");
  await page.getByLabel("City").fill("Colombo");
  await page.getByLabel("Postal code").fill("00300");
  await page.getByLabel(/^Country code/).fill("LK");
  await page.getByRole("button", { name: "Save address" }).click();

  await expect(page).toHaveURL(/\/checkout\?variant=/);
  await expect(page.getByRole("radio", { name: /Nadia Perera/ })).toBeChecked();
  await page.getByRole("radio", { name: new RegExp(shipping.name) }).check();
  await expect(page.getByTestId("checkout-subtotal")).toHaveText("$92.00");

  await page.getByLabel("Promo code").fill(code.toLowerCase());
  await page.getByRole("button", { name: "Apply" }).click();
  await expect(page.getByText(`${code} applied.`)).toBeVisible();
  await expect(page.getByTestId("checkout-discount")).toHaveText("-$9.20");
  await expect(page.getByTestId("checkout-total")).toHaveText("$87.80");
  await expect(summary.getByRole("list", { name: "Still needed" })).toHaveCount(0);
  await expect(page.getByRole("button", { name: "Payment coming soon" })).toBeDisabled();
});

test("a bad promo code is explained and nothing is taken off", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("badcode"));
  await page.goto(`${STORE}/shop/field-pocket-cargo`);
  await page.getByRole("button", { name: "Order Now" }).click();
  await page.getByLabel("Promo code").fill("NOTREAL");
  await page.getByRole("button", { name: "Apply" }).click();

  await expect(page.getByText("This promotion code can not be used on this order")).toBeVisible();
  await expect(page.getByTestId("checkout-discount")).toHaveCount(0);
});

test("signed out visitors are sent to sign in", async ({ page }) => {
  await page.goto(`${STORE}/checkout`);
  await expect(page).toHaveURL(/\/login\?next=%2Fcheckout$/);
});
