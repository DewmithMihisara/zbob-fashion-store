import { expect, test } from "@playwright/test";
import { API, STORE } from "./helpers";
import { adminToken, registerInBrowser, uniqueEmail } from "./customer-helpers";

test("a customer sees their own order, and another customer can not", async ({ page, browser, request }) => {
  const email = uniqueEmail("history");
  await registerInBrowser(page, email, "History Shopper");
  await page.goto(`${STORE}/account/orders`);
  await expect(page.getByText("You have no orders yet.")).toBeVisible();

  const token = await adminToken(request);
  const headers = { Authorization: `Bearer ${token}` };
  const customers = (await (await request.get(`${API}/admin/customers?size=100&sort=createdAt,desc`, { headers })).json()) as { content: { id: number; email: string }[] };
  const customer = customers.content.find((c) => c.email === email)!;
  const product = (await (await request.get(`${API}/products/terry-lounge-set`)).json()) as { options: { variantId: number }[] };
  const shipping = (await (
    await request.post(`${API}/admin/shipping-methods`, { headers, data: { name: `Courier ${Date.now()}`, priceCents: 500, estimatedDays: 3, active: true } })
  ).json()) as { id: number };
  const order = (await (
    await request.post(`${API}/admin/orders`, {
      headers,
      data: {
        customerId: customer.id,
        shippingMethodId: shipping.id,
        items: [{ variantId: product.options[0]!.variantId, quantity: 2 }],
        address: { recipient: "History Shopper", line1: "1 Main St", city: "Town", postalCode: "1000", country: "LK" },
      },
    })
  ).json()) as { id: number; orderNumber: string };

  await page.reload();
  await page.getByRole("link", { name: new RegExp(order.orderNumber) }).click();
  await expect(page.getByRole("heading", { name: order.orderNumber })).toBeVisible();
  await expect(page.getByText("Terry Lounge Set")).toBeVisible();
  await expect(page.getByText("Awaiting payment", { exact: false })).toBeVisible();
  await expect(page.getByText("$55.00")).toBeVisible();

  const other = await (await browser.newContext()).newPage();
  await registerInBrowser(other, uniqueEmail("nosy"));
  await other.goto(`${STORE}/account/orders/${order.id}`);
  await expect(other.getByTestId("form-error")).toContainText("Order not found");
  await expect(other.getByText("Terry Lounge Set")).toHaveCount(0);
});
