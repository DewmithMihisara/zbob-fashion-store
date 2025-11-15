import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";

test("clicking Order Now causes no navigation and no request", async ({ page }) => {
  await page.goto(`${STORE}/shop/everyday-square-tee`);
  await page.waitForLoadState("networkidle");
  const urlBefore = page.url();

  const requests: string[] = [];
  page.on("request", (request) => requests.push(request.url()));

  const button = page.getByRole("button", { name: "Order Now" });
  await expect(button).toBeEnabled();
  await button.click();
  await page.waitForTimeout(1500);

  expect(page.url()).toBe(urlBefore);
  expect(requests.filter((url) => !url.includes("_next") && !url.includes("__nextjs"))).toEqual([]);
  await expect(button).toBeVisible();
});
