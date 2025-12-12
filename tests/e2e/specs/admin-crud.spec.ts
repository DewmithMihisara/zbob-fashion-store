import { expect, test, type Page } from "@playwright/test";
import { ADMIN, errorAlert, signIn } from "./helpers";

const stamp = () => Date.now().toString(36);

function dialog(page: Page) {
  return page.getByRole("dialog");
}

async function deleteRow(page: Page, text: string) {
  const row = page.getByRole("row").filter({ hasText: text });
  await row.getByRole("button", { name: "Delete", exact: true }).click();
  await row.getByRole("button", { name: "Yes", exact: true }).click();
  await expect(row).toHaveCount(0);
}

test.beforeEach(async ({ page }) => {
  await signIn(page);
});

test("customers: add, edit and delete", async ({ page }) => {
  const id = stamp();
  await page.goto(`${ADMIN}/customers`);

  await page.getByRole("button", { name: "Add customer" }).click();
  await dialog(page).getByLabel("Full name").fill(`Cust ${id}`);
  await dialog(page).getByLabel("Email").fill(`cust-${id}@zbob.test`);
  await dialog(page).getByLabel("Password").fill("customer-password");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  const row = page.getByRole("row").filter({ hasText: `Cust ${id}` });
  await expect(row).toBeVisible();

  await row.getByRole("button", { name: "Edit" }).click();
  await dialog(page).getByLabel("Full name").fill(`Renamed ${id}`);
  await dialog(page).getByLabel("Can sign in").uncheck();
  await dialog(page).getByRole("button", { name: "Save" }).click();
  const renamed = page.getByRole("row").filter({ hasText: `Renamed ${id}` });
  await expect(renamed.getByText("Disabled")).toBeVisible();

  await deleteRow(page, `Renamed ${id}`);
});

test("subscribers: add, edit and delete", async ({ page }) => {
  const id = stamp();
  await page.goto(`${ADMIN}/subscribers`);

  await page.getByRole("button", { name: "Add subscriber" }).click();
  await dialog(page).getByLabel("Email").fill(`sub-${id}@zbob.test`);
  await dialog(page).getByRole("button", { name: "Save" }).click();
  const row = page.getByRole("row").filter({ hasText: `sub-${id}` });
  await expect(row).toBeVisible();

  await row.getByRole("button", { name: "Edit" }).click();
  await dialog(page).getByLabel("Email").fill(`changed-${id}@zbob.test`);
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("row").filter({ hasText: `changed-${id}` })).toBeVisible();

  await deleteRow(page, `changed-${id}`);
});

test("messages: add, edit and delete", async ({ page }) => {
  const id = stamp();
  await page.goto(`${ADMIN}/messages`);

  await page.getByRole("button", { name: "Add message" }).click();
  await dialog(page).getByLabel("Name").fill("Phone Caller");
  await dialog(page).getByLabel("Email").fill("caller@example.com");
  await dialog(page).getByLabel("Subject").fill(`Call ${id}`);
  await dialog(page).getByLabel("Message").fill("Asked about sizes.");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  const item = page.getByRole("listitem").filter({ hasText: `Call ${id}` });
  await expect(item).toBeVisible();

  await item.getByRole("button", { name: "Edit" }).click();
  await dialog(page).getByLabel("Message").fill("Asked about returns.");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(item.getByText("Asked about returns.")).toBeVisible();

  await item.getByRole("button", { name: "Delete" }).click();
  await item.getByRole("button", { name: "Yes" }).click();
  await expect(item).toHaveCount(0);
});

test("staff: add and delete, and you can not delete yourself", async ({ page }) => {
  const id = stamp();
  await page.goto(`${ADMIN}/staff`);

  await page.getByRole("button", { name: "Add staff member" }).first().click();
  await dialog(page).getByLabel("Full name").fill(`Helper ${id}`);
  await dialog(page).getByLabel("Email").fill(`helper-${id}@zbob.test`);
  await dialog(page).getByLabel("Password").fill("helper-password");
  await dialog(page).getByRole("button", { name: "Add staff member" }).click();
  await expect(page.getByRole("row").filter({ hasText: `Helper ${id}` })).toBeVisible();

  await deleteRow(page, `Helper ${id}`);

  const self = page.getByRole("row").filter({ hasText: "e2e-admin@zbob.test" });
  await self.getByRole("button", { name: "Delete", exact: true }).click();
  await self.getByRole("button", { name: "Yes", exact: true }).click();
  await expect(errorAlert(page)).toHaveText("You can not delete your own account");
});

test("stock page: add, edit and delete a row", async ({ page }) => {
  const id = stamp();
  await page.goto(`${ADMIN}/inventory`);

  await page.getByRole("button", { name: "Add stock row" }).click();
  await dialog(page).getByLabel("Product").selectOption({ label: "Everyday Square Tee" });
  await dialog(page).getByLabel("SKU").fill(`SKU-${id}`);
  await dialog(page).getByLabel("Size").fill("XXL");
  await dialog(page).getByLabel("Colour").fill(`Tone ${id}`);
  await dialog(page).getByLabel("In stock").fill("3");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(dialog(page)).toHaveCount(0);

  await page.getByRole("checkbox", { name: "Show only low and out of stock" }).check();
  const row = page.getByRole("row").filter({ hasText: `SKU-${id}` });
  await expect(row).toBeVisible();

  await row.getByRole("button", { name: "Edit" }).click();
  await dialog(page).getByLabel("SKU").fill(`SKU2-${id}`);
  await dialog(page).getByRole("button", { name: "Save" }).click();

  await deleteRow(page, `SKU2-${id}`);
});

test("products and blog tables delete from the row", async ({ page }) => {
  const id = stamp();

  await page.goto(`${ADMIN}/products/new`);
  await page.getByLabel("Name").fill(`Row Tee ${id}`);
  await page.getByLabel("Description").fill("Made by a test.");
  await page.getByLabel("Price", { exact: true }).fill("10");
  await page.getByLabel("Category").selectOption({ label: "Men's Wear" });
  await page.getByRole("button", { name: "Create product" }).click();
  await expect(page.getByRole("heading", { name: `Row Tee ${id}` })).toBeVisible();
  await page.goto(`${ADMIN}/products`);
  await page.getByLabel("Search products").fill(`Row Tee ${id}`);
  await deleteRow(page, `Row Tee ${id}`);

  await page.goto(`${ADMIN}/blog/new`);
  await page.getByLabel("Title").fill(`Row Post ${id}`);
  await page.getByLabel("Short summary").fill("Summary");
  await page.getByLabel("Post", { exact: true }).fill("Body");
  await page.getByRole("button", { name: "Create post" }).click();
  await expect(page.getByRole("heading", { name: `Row Post ${id}` })).toBeVisible();
  await page.goto(`${ADMIN}/blog`);
  await deleteRow(page, `Row Post ${id}`);
});

test("orders: add, edit address, cancel and delete", async ({ page }) => {
  const id = stamp();

  await page.goto(`${ADMIN}/customers`);
  await page.getByRole("button", { name: "Add customer" }).click();
  await dialog(page).getByLabel("Full name").fill(`Buyer ${id}`);
  await dialog(page).getByLabel("Email").fill(`buyer-${id}@zbob.test`);
  await dialog(page).getByLabel("Password").fill("customer-password");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("row").filter({ hasText: `Buyer ${id}` })).toBeVisible();

  await page.goto(`${ADMIN}/shipping`);
  await page.getByRole("button", { name: "Add method" }).click();
  await dialog(page).getByLabel("Name").fill(`Ship ${id}`);
  await dialog(page).getByLabel("Price").fill("5");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(page.getByRole("row").filter({ hasText: `Ship ${id}` })).toBeVisible();

  await page.goto(`${ADMIN}/orders/new`);
  const form = page.locator("form");
  await form.getByLabel("Customer").selectOption({ label: `Buyer ${id} (buyer-${id}@zbob.test)` });
  await form.getByLabel("Item 1").selectOption({ index: 1 });
  await form.getByLabel("Delivery", { exact: true }).selectOption({ label: `Ship ${id}` });
  await form.getByLabel("Recipient").fill("Sam Buyer");
  await form.getByLabel("Address line 1").fill("1 Main St");
  await form.getByLabel("City").fill("Town");
  await form.getByLabel("Postal code").fill("1000");
  await form.getByLabel("Country code").fill("lk");
  await page.getByRole("button", { name: "Create order" }).click();
  await expect(page.getByRole("heading", { name: /^ZB-/ })).toBeVisible();
  await expect(page.getByText("Sam Buyer")).toBeVisible();

  await page.getByRole("button", { name: "Edit address" }).click();
  await dialog(page).getByLabel("Recipient").fill("Alex Buyer");
  await dialog(page).getByRole("button", { name: "Save" }).click();
  await expect(page.getByText("Alex Buyer")).toBeVisible();

  await page.getByRole("button", { name: "Mark as cancelled" }).click();
  await expect(page.getByRole("button", { name: "Delete order" })).toBeVisible();
  await page.getByRole("button", { name: "Delete order" }).click();
  await page.getByRole("button", { name: "Confirm delete" }).click();
  await expect(page).toHaveURL(/\/orders$/);
});
