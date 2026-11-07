import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";
import { asAdmin, PASSWORD, registerInBrowser, uniqueEmail } from "./customer-helpers";

const SLUG = "everyday-square-tee";

test("signed out, Order Now asks for sign in and then opens checkout for that size", async ({ page }) => {
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "L", exact: true }).click();
  await page.getByRole("button", { name: "Order Now" }).click();
  await expect(page).toHaveURL(/\/login\?next=%2Fcheckout%3Fvariant%3D\d+%26qty%3D1$/);

  await page.getByRole("link", { name: "Create an account" }).click();
  await page.getByLabel("Full name").fill("Quick Buyer");
  await page.getByRole("main").getByLabel("Email").fill(uniqueEmail("quick"));
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Create account" }).click();

  await expect(page).toHaveURL(/\/checkout\?variant=\d+&qty=1$/);
  await expect(page.getByRole("complementary", { name: "Order summary" })).toContainText("Everyday Square Tee");
  await expect(page.getByRole("complementary", { name: "Order summary" })).toContainText("L / Black");
});

test("checkout stops before payment and never creates an order", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("nopay"));
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "Order Now" }).click();
  await expect(page).toHaveURL(/\/checkout\?variant=/);

  const pay = page.getByRole("button", { name: "Payment coming soon" });
  await expect(pay).toBeDisabled();
  await expect(page.getByText("Online payment is not open yet.")).toBeVisible();

  await page.goto(`${STORE}/account/orders`);
  await expect(page.getByText("You have no orders yet.")).toBeVisible();
});

test("a sold out product cannot be ordered", async ({ page, request }) => {
  const admin = await asAdmin(request);
  const name = `Sold Out Tee ${Date.now()}`;
  const categoryId = (await admin.get<{ content: { id: number }[] }>("/admin/categories")).content[0]!.id;
  const product = await admin.post<{ id: number; slug: string }>("/admin/products", {
    name,
    description: "Gone.",
    priceCents: 1000,
    published: true,
    categoryId,
  });

  await page.goto(`${STORE}/shop/${product.slug}`);
  await expect(page.getByRole("button", { name: "Out of stock" })).toBeDisabled();
});
