import { expect, type Page } from "@playwright/test";

export const STORE = process.env.E2E_STORE_URL ?? "http://localhost:3000";
export const ADMIN = process.env.E2E_ADMIN_URL ?? "http://localhost:3001";
export const API = process.env.E2E_API_URL ?? "http://localhost:8080/api/v1";

export const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL ?? "e2e-admin@zbob.test";
export const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? "e2e-admin-password";

/** A valid 1x1 PNG, enough to exercise the whole upload path. */
export const PNG = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M/wHwAEBgIApD5fRAAAAABJRU5ErkJggg==",
  "base64",
);

export async function signIn(page: Page): Promise<void> {
  await page.goto(`${ADMIN}/login`);
  // The session lasts for the tab, so a second call lands straight on the dashboard.
  const signInButton = page.getByRole("button", { name: "Sign in" });
  const dashboard = page.getByRole("heading", { name: "Dashboard" });
  await expect(signInButton.or(dashboard)).toBeVisible();
  if (await signInButton.isVisible()) {
    await page.getByLabel("Email").fill(ADMIN_EMAIL);
    await page.getByLabel("Password").fill(ADMIN_PASSWORD);
    await signInButton.click();
  }
  await expect(dashboard).toBeVisible();
}

/** The page's own error, not the hidden alert Next.js uses to announce route changes. */
export function errorAlert(page: Page) {
  return page.locator("main p[role=alert]");
}

/** Opens a product in the admin editor through the products list. */
export async function openProduct(page: Page, name: string): Promise<void> {
  await page.goto(`${ADMIN}/products`);
  await page.getByLabel("Search products").fill(name);
  await page.getByRole("link", { name, exact: true }).click();
  await expect(page.getByRole("heading", { name, exact: true })).toBeVisible();
}
