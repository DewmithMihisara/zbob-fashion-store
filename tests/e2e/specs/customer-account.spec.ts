import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";
import { PASSWORD, registerInBrowser, uniqueEmail } from "./customer-helpers";

test("account pages send signed out visitors to sign in and back", async ({ page }) => {
  await page.goto(`${STORE}/account/addresses`);
  await expect(page).toHaveURL(/\/login\?next=%2Faccount%2Faddresses$/);
});

test("sign up, sign out and sign back in", async ({ page }) => {
  const email = uniqueEmail("signup");
  await registerInBrowser(page, email, "Nadia Perera");
  await expect(page).toHaveURL(/\/account$/);
  await expect(page.getByRole("heading", { name: "Hello, Nadia Perera" })).toBeVisible();

  await page.getByRole("button", { name: "Sign out" }).click();
  await expect(page).toHaveURL(/\/login/);
  await expect(page.getByRole("link", { name: "Sign in" }).first()).toBeVisible();

  await page.getByRole("main").getByLabel("Email").fill(email);
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByRole("heading", { name: "Hello, Nadia Perera" })).toBeVisible();
});

test("the login survives reloading the page", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("reload"));
  await page.reload();
  await expect(page.getByRole("link", { name: "Your account" })).toBeVisible();
});

test("signing up with a taken email is explained", async ({ page, browser }) => {
  const email = uniqueEmail("taken");
  await registerInBrowser(page, email);

  const other = await (await browser.newContext()).newPage();
  await other.goto(`${STORE}/register`);
  await other.getByLabel("Full name").fill("Someone Else");
  await other.getByRole("main").getByLabel("Email").fill(email);
  await other.getByLabel("Password").fill(PASSWORD);
  await other.getByRole("button", { name: "Create account" }).click();
  await expect(other.getByTestId("form-error")).toHaveText("An account with this email already exists");
});

test("wrong password is refused", async ({ page }) => {
  await page.goto(`${STORE}/login`);
  await page.getByRole("main").getByLabel("Email").fill("nobody@zbob.test");
  await page.getByLabel("Password").fill("wrong-password");
  await page.getByRole("button", { name: "Sign in" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("Invalid email or password");
});

test("a crafted return link can not leave the site", async ({ page }) => {
  await page.goto(`${STORE}/register?next=${encodeURIComponent("//evil.example")}`);
  await page.getByLabel("Full name").fill("Safe Shopper");
  await page.getByRole("main").getByLabel("Email").fill(uniqueEmail("safe"));
  await page.getByLabel("Password").fill(PASSWORD);
  await page.getByRole("button", { name: "Create account" }).click();
  await expect(page).toHaveURL(`${STORE}/account`);
});

test("edit name and change password", async ({ page }) => {
  const email = uniqueEmail("profile");
  await registerInBrowser(page, email);

  await page.getByLabel("Full name").fill("New Name");
  await page.getByRole("button", { name: "Save details" }).click();
  await expect(page.getByRole("heading", { name: "Hello, New Name" })).toBeVisible();

  await page.getByLabel("Current password").fill("wrong-password");
  await page.getByLabel("New password").fill("another-password");
  await page.getByRole("button", { name: "Change password" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("Your current password is not correct");

  await page.getByLabel("Current password").fill(PASSWORD);
  await page.getByRole("button", { name: "Change password" }).click();
  await expect(page.getByText("Your password was changed.")).toBeVisible();
});

test("add, edit and remove an address", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("address"));
  await page.getByRole("link", { name: "Addresses" }).click();
  await expect(page.getByText("You have no saved addresses yet.")).toBeVisible();

  await page.getByRole("button", { name: "Add an address" }).click();
  await page.getByLabel("Recipient").fill("Nadia Perera");
  await page.getByLabel("Address line 1").fill("12 Galle Road");
  await page.getByLabel("City").fill("Colombo");
  await page.getByLabel("Postal code").fill("00300");
  await page.getByLabel(/^Country code/).fill("lk");
  await page.getByRole("button", { name: "Save address" }).click();
  const card = page.getByRole("listitem").filter({ hasText: "12 Galle Road" });
  await expect(card).toContainText("LK");

  await card.getByRole("button", { name: "Edit" }).click();
  await page.getByLabel("City").fill("Kandy");
  await page.getByRole("button", { name: "Save address" }).click();
  await expect(card).toContainText("Kandy");

  await card.getByRole("button", { name: "Remove" }).click();
  await expect(page.getByText("You have no saved addresses yet.")).toBeVisible();
});
