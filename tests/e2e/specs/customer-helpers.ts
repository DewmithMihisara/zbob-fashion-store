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

/** Calls the admin API as the test admin. */
export async function asAdmin(request: APIRequestContext) {
  const headers = { Authorization: `Bearer ${await adminToken(request)}` };
  return {
    get: async <T>(path: string) => (await (await request.get(`${API}${path}`, { headers })).json()) as T,
    post: async <T>(path: string, data?: unknown) => (await (await request.post(`${API}${path}`, { headers, data })).json()) as T,
    patch: async (path: string, data: unknown) => request.patch(`${API}${path}`, { headers, data }),
  };
}

/** Creates a delivery option with a unique name and returns it. */
export async function newShippingMethod(request: APIRequestContext, priceCents = 500) {
  const admin = await asAdmin(request);
  return admin.post<{ id: number; name: string }>("/admin/shipping-methods", {
    name: `Courier ${Date.now().toString(36)}`,
    priceCents,
    estimatedDays: 2,
    active: true,
  });
}

/** Places an order for a customer through the admin API and returns it. */
export async function newOrderFor(request: APIRequestContext, email: string, slug: string) {
  const admin = await asAdmin(request);
  const customers = await admin.get<{ content: { id: number; email: string }[] }>("/admin/customers?size=100&sort=createdAt,desc");
  const customer = customers.content.find((c) => c.email === email)!;
  const product = (await (await request.get(`${API}/products/${slug}`)).json()) as { options: { variantId: number; inStock: boolean }[] };
  const shipping = await newShippingMethod(request);
  return admin.post<{ id: number; orderNumber: string }>("/admin/orders", {
    customerId: customer.id,
    shippingMethodId: shipping.id,
    items: [{ variantId: product.options.find((o) => o.inStock)!.variantId, quantity: 1 }],
    address: { recipient: "Test Shopper", line1: "1 Main St", city: "Town", postalCode: "1000", country: "LK" },
  });
}
