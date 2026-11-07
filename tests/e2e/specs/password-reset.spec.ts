import { readFileSync } from "node:fs";
import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";
import { PASSWORD, registerInBrowser, uniqueEmail } from "./customer-helpers";

/** Emails go to the API log until a provider is set up, so the test reads the link from there. */
const API_LOG = process.env.E2E_API_LOG;

async function resetLinkFor(email: string): Promise<string> {
  for (let attempt = 0; attempt < 20; attempt++) {
    const log = readFileSync(API_LOG!, "utf8");
    const start = log.lastIndexOf(`Email to ${email} | Reset your zbob password`);
    const match = start >= 0 ? log.slice(start).match(/reset-password\?token=([A-Za-z0-9_-]+)/) : null;
    if (match) {
      return `/reset-password?token=${match[1]}`;
    }
    await new Promise((resolve) => setTimeout(resolve, 250));
  }
  throw new Error(`No reset email for ${email} in ${API_LOG}`);
}

test("forgot password sends a link that sets a new password once", async ({ page }) => {
  test.skip(!API_LOG, "Set E2E_API_LOG to the API's log file to read the reset email");
  const email = uniqueEmail("forgot");
  await registerInBrowser(page, email);
  await page.goto(`${STORE}/account`);
  await page.getByRole("button", { name: "Sign out" }).click();

  await page.getByRole("link", { name: "Forgot your password?" }).click();
  // The sign in form has an Email field too, so wait until the new page has replaced it.
  await expect(page.getByRole("heading", { name: "Forgot your password?" })).toBeVisible();
  await page.getByRole("main").getByLabel("Email").fill(email);
  await page.getByRole("button", { name: "Send reset link" }).click();
  await expect(page.getByRole("heading", { name: "Check your email" })).toBeVisible();

  const link = await resetLinkFor(email);
  await page.goto(`${STORE}${link}`);
  await page.getByLabel(/^New password/).fill("brand-new-password");
  await page.getByLabel("Repeat new password").fill("brand-new-password");
  await page.getByRole("button", { name: "Save new password" }).click();
  await expect(page.getByRole("heading", { name: "Password changed" })).toBeVisible();

  await page.getByRole("link", { name: "Sign in" }).last().click();
  await page.getByRole("main").getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill("brand-new-password");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: /^Hello/ })).toBeVisible();

  await page.goto(`${STORE}${link}`);
  await page.getByLabel(/^New password/).fill("third-password");
  await page.getByLabel("Repeat new password").fill("third-password");
  await page.getByRole("button", { name: "Save new password" }).click();
  await expect(page.getByTestId("form-error")).toContainText("invalid or has expired");
});

test("an unknown email gets the same answer", async ({ page }) => {
  await page.goto(`${STORE}/forgot-password`);
  await page.getByRole("main").getByLabel("Email").fill(uniqueEmail("ghost"));
  await page.getByRole("button", { name: "Send reset link" }).click();
  await expect(page.getByRole("heading", { name: "Check your email" })).toBeVisible();
});

test("mismatched new passwords are caught before sending", async ({ page }) => {
  await page.goto(`${STORE}/reset-password?token=anything`);
  await page.getByLabel(/^New password/).fill("one-password");
  await page.getByLabel("Repeat new password").fill("another-password");
  await page.getByRole("button", { name: "Save new password" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("The two passwords do not match.");
});

test("repeated wrong passwords lock sign in for a while", async ({ page }) => {
  const email = uniqueEmail("locked");
  await registerInBrowser(page, email);
  await page.goto(`${STORE}/account`);
  await page.getByRole("button", { name: "Sign out" }).click();

  for (let i = 0; i < 5; i++) {
    await page.getByRole("main").getByLabel("Email").fill(email);
    await page.getByLabel("Password").fill(`wrong-${i}`);
    await page.getByRole("button", { name: "Sign in" }).click();
    await expect(page.getByTestId("form-error")).toHaveText("Invalid email or password");
  }

  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("Too many attempts. Please try again in 15 minutes.");
});
