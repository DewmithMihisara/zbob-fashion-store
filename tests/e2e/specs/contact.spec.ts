import { expect, test } from "@playwright/test";
import { ADMIN, signIn, STORE } from "./helpers";

test("a contact message reaches the dashboard", async ({ page }) => {
  const subject = `Sizing question ${Date.now()}`;

  await page.goto(`${STORE}/contact`);
  await page.getByLabel("Full name").fill("Sam Tester");
  await page.getByLabel("Email", { exact: true }).fill("sam@example.com");
  await page.getByLabel("Subject").fill(subject);
  await page.getByLabel("Message").fill("Does this run large?");
  await page.getByRole("button", { name: "Send Message" }).click();
  await expect(page.getByText("Thanks, your message has been sent.")).toBeVisible();

  await signIn(page);
  await page.goto(`${ADMIN}/messages`);
  await expect(page.getByText(subject)).toBeVisible();
});

test("a newsletter signup reaches the dashboard", async ({ page }) => {
  const email = `news-${Date.now()}@example.com`;

  await page.goto(STORE);
  await page.getByLabel("Email address").fill(email);
  await page.getByRole("button", { name: "Subscribe" }).click();
  await expect(page.getByText("Thanks, you are on the list.")).toBeVisible();

  await signIn(page);
  await page.goto(`${ADMIN}/subscribers`);
  await expect(page.getByRole("cell", { name: email })).toBeVisible();
});
