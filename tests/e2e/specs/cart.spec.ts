import { expect, test } from "@playwright/test";
import { API, STORE } from "./helpers";
import { adminToken, PASSWORD, registerInBrowser, uniqueEmail } from "./customer-helpers";

const PRODUCT = "Field Pocket Cargo";
const SLUG = "field-pocket-cargo";

test("signed out shoppers are sent to sign in, then back to the product", async ({ page }) => {
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page).toHaveURL(/\/login\?next=%2Fshop%2Ffield-pocket-cargo$/);

  await page.getByRole("link", { name: "Create an account" }).click();
  await page.getByLabel("Full name").fill("Return Shopper");
  await page.getByRole("main").getByLabel("Email").fill(uniqueEmail("return"));
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Create account" }).click();

  await expect(page).toHaveURL(new RegExp(`/shop/${SLUG}$`));
});

test("the cart page asks signed out visitors to sign in", async ({ page }) => {
  await page.goto(`${STORE}/cart`);
  await expect(page.getByText("Sign in to see your cart.")).toBeVisible();
});

test("add a chosen size, change quantity, then remove it", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("cart"));
  await page.goto(`${STORE}/shop/${SLUG}`);

  await page.getByRole("button", { name: "L", exact: true }).click();
  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByText("Added to your cart.")).toBeVisible();
  await expect(page.getByRole("link", { name: "Cart, 1 item" })).toBeVisible();

  await page.getByRole("link", { name: "View cart" }).click();
  const line = page.getByRole("listitem").filter({ hasText: PRODUCT });
  await expect(line).toContainText("L / Black");
  await expect(page.getByTestId("subtotal")).toHaveText("$92.00");

  await line.getByRole("button", { name: "Increase quantity" }).click();
  await expect(line.getByTestId("quantity")).toHaveText("2");
  await expect(page.getByTestId("subtotal")).toHaveText("$184.00");
  await expect(page.getByRole("link", { name: "Cart, 2 items" })).toBeVisible();

  await line.getByRole("button", { name: "Decrease quantity" }).click();
  await expect(line.getByTestId("quantity")).toHaveText("1");

  await line.getByRole("button", { name: "Remove" }).click();
  await expect(page.getByText("Your cart is empty.")).toBeVisible();
  await expect(page.getByRole("link", { name: "Cart", exact: true })).toBeVisible();
});

test("the cart stays on the account after signing out and in", async ({ page }) => {
  const email = uniqueEmail("keep");
  await registerInBrowser(page, email);
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByRole("link", { name: "Cart, 1 item" })).toBeVisible();

  await page.goto(`${STORE}/account`);
  await page.getByRole("button", { name: "Sign out" }).click();
  await expect(page.getByRole("link", { name: "Cart", exact: true })).toBeVisible();

  await page.getByRole("main").getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("link", { name: "Cart, 1 item" })).toBeVisible();
});

test("you can not add more than is in stock", async ({ page, request }) => {
  const token = await adminToken(request);
  const headers = { Authorization: `Bearer ${token}` };
  const name = `Scarce Tee ${Date.now()}`;
  const categories = (await (await request.get(`${API}/categories`)).json()) as { id: number }[];
  const product = (await (
    await request.post(`${API}/admin/products`, {
      headers,
      data: { name, description: "Limited.", priceCents: 1000, published: true, categoryId: categories[0]!.id },
    })
  ).json()) as { id: number; slug: string };
  await request.post(`${API}/admin/products/${product.id}/variants`, {
    headers,
    data: { sku: `SC-${Date.now()}`, size: "M", color: "Black", stockQuantity: 1, lowStockThreshold: 0 },
  });

  await registerInBrowser(page, uniqueEmail("scarce"));
  await page.goto(`${STORE}/shop/${product.slug}`);
  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByRole("link", { name: "Cart, 1 item" })).toBeVisible();

  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("Only 1 left in stock");
});

test("checkout stays unavailable and Order Now stays inert when signed in", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("inert"));
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByRole("link", { name: "Cart, 1 item" })).toBeVisible();
  await page.waitForLoadState("networkidle");

  const urlBefore = page.url();
  const requests: string[] = [];
  page.on("request", (r) => requests.push(r.url()));
  await page.getByRole("button", { name: "Order Now" }).click();
  await page.waitForTimeout(1000);
  expect(page.url()).toBe(urlBefore);
  expect(requests.filter((url) => !url.includes("_next"))).toEqual([]);

  await page.goto(`${STORE}/cart`);
  await expect(page.getByRole("button", { name: "Checkout coming soon" })).toBeDisabled();
});
