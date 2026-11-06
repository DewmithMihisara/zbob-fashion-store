import { expect, test } from "@playwright/test";
import { ADMIN, signIn } from "./helpers";

test("reports show headline figures, daily charts and a table view", async ({ page }) => {
  await signIn(page);
  await page.getByRole("link", { name: "Reports" }).click();

  await expect(page.getByRole("heading", { name: "Reports" })).toBeVisible();
  await expect(page.getByText("Average paid order")).toBeVisible();
  await expect(page.getByRole("img", { name: "Revenue by day, last 30 days" })).toBeVisible();
  await expect(page.getByRole("img", { name: "Orders by day, last 30 days" })).toBeVisible();
  await expect(page.getByRole("list", { name: "Best sellers by units sold" })).toBeVisible();
  await expect(page.getByText("In stock").first()).toBeVisible();

  await page.getByRole("button", { name: "Last 7 days" }).click();
  await expect(page.getByRole("img", { name: "Revenue by day, last 7 days" })).toBeVisible();

  await page.getByRole("button", { name: "Show as table" }).click();
  await expect(page.getByRole("row")).toHaveCount(8);
});

test("hovering a day shows its value", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/reports`);
  const chart = page.getByRole("img", { name: "Orders by day, last 30 days" });
  await chart.locator("rect").last().hover();
  await expect(page.getByRole("tooltip")).toContainText(/\d+ orders?/);
});
