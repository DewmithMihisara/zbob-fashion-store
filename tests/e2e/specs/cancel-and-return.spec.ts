import { expect, test } from "@playwright/test";
import { ADMIN, signIn, STORE } from "./helpers";
import { asAdmin, newOrderFor, registerInBrowser, uniqueEmail } from "./customer-helpers";

test("a customer can cancel an order that is waiting for payment", async ({ page, request }) => {
  const email = uniqueEmail("canceller");
  await registerInBrowser(page, email);
  const order = await newOrderFor(request, email, "mini-rugby-stripe");

  await page.goto(`${STORE}/account/orders/${order.id}`);
  await page.getByRole("button", { name: "Cancel order" }).click();
  await page.getByRole("button", { name: "Yes, cancel it" }).click();

  await expect(page.getByText(/Cancelled/).first()).toBeVisible();
  await expect(page.getByRole("button", { name: "Cancel order" })).toHaveCount(0);
});

test("a delivered order can be returned, and staff approve it with a note", async ({ page, browser, request }) => {
  const email = uniqueEmail("returner");
  await registerInBrowser(page, email);
  const order = await newOrderFor(request, email, "mini-rugby-stripe");
  const admin = await asAdmin(request);
  for (const status of ["PAID", "SHIPPED", "DELIVERED"]) {
    await admin.patch(`/admin/orders/${order.id}/status`, { status });
  }

  await page.goto(`${STORE}/account/orders/${order.id}`);
  await expect(page.getByRole("button", { name: "Cancel order" })).toHaveCount(0);
  await page.getByLabel("What would you like to return, and why?").fill("The stripe is too wide for me.");
  await page.getByRole("button", { name: "Request a return" }).click();
  await expect(page.getByText("Waiting for a reply")).toBeVisible();
  await expect(page.getByRole("button", { name: "Request a return" })).toHaveCount(0);

  const staffPage = await (await browser.newContext()).newPage();
  await signIn(staffPage);
  await staffPage.goto(`${ADMIN}/returns`);
  const row = staffPage.getByRole("row").filter({ hasText: order.orderNumber });
  await expect(row).toContainText("The stripe is too wide for me.");
  await row.getByRole("button", { name: "Approve" }).click();
  await staffPage.getByLabel(/^Note to the customer/).fill("Post it back within 14 days.");
  await staffPage.getByRole("dialog").getByRole("button", { name: "Approve" }).click();
  await expect(staffPage.getByRole("dialog")).toHaveCount(0);

  await page.reload();
  await expect(page.getByText("Approved", { exact: false }).first()).toBeVisible();
  await expect(page.getByText("Note from zbob: Post it back within 14 days.")).toBeVisible();
});
