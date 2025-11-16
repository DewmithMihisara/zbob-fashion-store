import { expect, test } from "@playwright/test";
import { ADMIN, ADMIN_EMAIL, API, errorAlert, signIn } from "./helpers";

test("dashboard pages send signed out visitors to login", async ({ page }) => {
  await page.goto(`${ADMIN}/products`);

  await expect(page).toHaveURL(/\/login$/);
});

test("wrong password shows an error and stays signed out", async ({ page }) => {
  await page.goto(`${ADMIN}/login`);
  await page.getByLabel("Email").fill(ADMIN_EMAIL);
  await page.getByLabel("Password").fill("definitely-wrong");
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(errorAlert(page)).toHaveText("Invalid email or password");
  await expect(page).toHaveURL(/\/login$/);
});

test("signing in and out works", async ({ page }) => {
  await signIn(page);

  await page.getByRole("button", { name: "Sign out" }).click();

  await expect(page).toHaveURL(/\/login$/);
});

test("admin api refuses anonymous and customer requests", async ({ request }) => {
  expect((await request.get(`${API}/admin/products`)).status()).toBe(401);

  const email = `customer-${Date.now()}@zbob.test`;
  const registered = await request.post(`${API}/auth/customer/register`, {
    data: { email, password: "customer-password", fullName: "E2E Customer" },
  });
  expect(registered.status()).toBe(201);
  const { accessToken } = (await registered.json()) as { accessToken: string };

  const asCustomer = await request.get(`${API}/admin/products`, { headers: { Authorization: `Bearer ${accessToken}` } });
  expect(asCustomer.status()).toBe(403);

  const stockChange = await request.patch(`${API}/admin/variants/1/stock`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    data: { stockQuantity: 999 },
  });
  expect(stockChange.status()).toBe(403);
});
