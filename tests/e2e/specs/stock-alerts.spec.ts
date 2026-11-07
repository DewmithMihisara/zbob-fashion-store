import { readFileSync } from "node:fs";
import { expect, test } from "@playwright/test";
import { API, STORE } from "./helpers";
import { asAdmin } from "./customer-helpers";

const SLUG = "junior-cable-knit";

test("a sold out size takes an email and is emailed once when it is back", async ({ page, request }) => {
  const admin = await asAdmin(request);
  const product = (await (await request.get(`${API}/products/${SLUG}`)).json()) as { options: { variantId: number; size: string }[] };
  const small = product.options.find((o) => o.size === "S")!;
  await admin.patch(`/admin/variants/${small.variantId}/stock`, { stockQuantity: 0 });
  const email = `alert-${Date.now()}@example.com`;

  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: "S, sold out" }).click();
  await expect(page.getByRole("button", { name: "Add to cart" })).toHaveCount(0);
  await page.getByLabel("Email for back in stock alert").fill(email);
  await page.getByRole("button", { name: "Notify me" }).click();
  await expect(page.getByText(`We will email ${email} once when S in Black is back.`)).toBeVisible();

  await admin.patch(`/admin/variants/${small.variantId}/stock`, { stockQuantity: 6 });

  const log = process.env.E2E_API_LOG;
  test.skip(!log, "Set E2E_API_LOG to check the email");
  await expect.poll(() => readFileSync(log!, "utf8").includes(`Email to ${email} | Junior Cable Knit is back in stock`)).toBe(true);

  await page.reload();
  await expect(page.getByRole("button", { name: "S", exact: true })).toBeVisible();
});
