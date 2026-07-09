import { readFileSync } from "node:fs";
import { expect, test } from "@playwright/test";
import { ADMIN, signIn } from "./helpers";

test("export downloads a dated csv with a header row", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory`);

  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Export CSV" }).click();
  const file = await download;

  expect(file.suggestedFilename()).toMatch(/^zbob-inventory-\d{4}-\d{2}-\d{2}\.csv$/);
  const text = readFileSync((await file.path())!, "utf8");
  expect(text.replace(/^﻿/, "").split("\r\n")[0]).toBe("variant_id,product,sku,size,colour,in_stock,low_stock_at");
  expect(text).toContain("Ribbed Merino Crew");
});

test("tick rows and change their stock together", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory`);
  const rows = page.getByRole("row").filter({ hasText: "Ribbed Merino Crew" });
  await expect(rows).toHaveCount(3);

  for (const label of ["S", "M", "L"]) {
    await page.getByLabel(`Tick Ribbed Merino Crew ${label} Black`).check();
  }
  await expect(page.getByText("3 selected")).toBeVisible();
  await page.getByLabel("Change type").selectOption("SET");
  await page.getByLabel("Amount").fill("33");
  await page.getByRole("button", { name: "Apply" }).click();

  await expect(page.getByText("3 selected")).toHaveCount(0);
  for (const label of ["S", "M", "L"]) {
    await expect(page.getByLabel(`Stock for Ribbed Merino Crew ${label} Black`)).toHaveValue("33");
  }

  await page.getByLabel("Tick Ribbed Merino Crew S Black").check();
  await page.getByLabel("Amount").fill("-50");
  await page.getByRole("button", { name: "Apply" }).click();
  await expect(page.getByRole("form", { name: "Change stock for ticked rows" }).getByRole("alert")).toContainText("below 0");
  await expect(page.getByLabel("Stock for Ribbed Merino Crew S Black")).toHaveValue("33");
});

test("the activity log shows dashboard changes, exports and sign ins", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/inventory`);
  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Export CSV" }).click();
  await download;

  await page.getByRole("link", { name: "Activity" }).click();
  await expect(page.getByRole("heading", { name: "Activity" })).toBeVisible();
  await expect(page.getByRole("row").filter({ hasText: "Exported inventory.csv" }).first()).toBeVisible();
  await expect(page.getByRole("row").filter({ hasText: "Signed in" }).first()).toContainText("e2e-admin@zbob.test");
});

test("staff can send a customer a reset link", async ({ page }) => {
  await signIn(page);
  await page.goto(`${ADMIN}/customers`);
  const row = page.getByRole("row").filter({ has: page.getByRole("button", { name: "Send reset link" }) }).first();
  const email = (await row.getByRole("cell").nth(1).innerText()).trim();

  await row.getByRole("button", { name: "Send reset link" }).click();

  await expect(page.getByRole("status")).toHaveText(`Reset link sent to ${email}.`);
});
