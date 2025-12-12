import { expect, test } from "@playwright/test";
import { ADMIN, errorAlert, openProduct, PNG, signIn } from "./helpers";

const png = (name: string) => ({ name, mimeType: "image/png", buffer: PNG });

test("a product takes five photos and no more", async ({ page }) => {
  await signIn(page);
  await openProduct(page, "Mini Rugby Stripe");
  const input = page.getByLabel("Choose image file");

  await input.setInputFiles(["1", "2", "3", "4", "5", "6"].map((n) => png(`${n}.png`)));
  await expect(errorAlert(page)).toContainText("You can add 5 more");
  await expect(page.getByRole("heading", { name: "Photos (0 of 5)" })).toBeVisible();

  await input.setInputFiles(["1", "2", "3", "4", "5"].map((n) => png(`${n}.png`)));
  await expect(page.getByRole("heading", { name: "Photos (5 of 5)" })).toBeVisible();
  await expect(page.getByRole("button", { name: "Limit reached" })).toBeDisabled();
});

test("the stock table moves between server pages", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory`);

  await expect(page.getByText(/Page 1 of \d+/)).toBeVisible();
  const firstPage = await page.getByRole("row").nth(1).innerText();

  const response = page.waitForResponse((r) => r.url().includes("/admin/inventory") && r.url().includes("page=1"));
  await page.getByRole("button", { name: "Next" }).click();
  await response;

  await expect(page.getByText(/Page 2 of \d+/)).toBeVisible();
  expect(await page.getByRole("row").nth(1).innerText()).not.toBe(firstPage);
});
