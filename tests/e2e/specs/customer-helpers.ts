import { expect, type APIRequestContext, type Page } from "@playwright/test";
import { ADMIN_EMAIL, ADMIN_PASSWORD, API, STORE } from "./helpers";

export const PASSWORD = "customer-password";

export function uniqueEmail(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 6)}@zbob.test`;
}

/** Creates an account through the website, which also signs in. */
export async function registerInBrowser(page: Page, email: string, name = "Test Shopper", next = ""): Promise<void> {
  await page.goto(`${STORE}/register${next ? `?next=${encodeURIComponent(next)}` : ""}`);
  await page.getByLabel("Full name").fill(name);
  await page.getByRole("main").getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Create account" }).click();
  await expect(page.getByRole("link", { name: "Your account" })).toBeVisible();
}

export async function adminToken(request: APIRequestContext): Promise<string> {
  const response = await request.post(`${API}/auth/admin/login`, { data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD } });
  return ((await response.json()) as { accessToken: string }).accessToken;
}
