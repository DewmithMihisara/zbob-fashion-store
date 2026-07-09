import { expect, test } from "@playwright/test";
import { STORE } from "./helpers";
import { registerInBrowser, uniqueEmail } from "./customer-helpers";

const PRODUCT = "Linen Weekend Dress";
const SLUG = "linen-weekend-dress";

test("signed out shoppers are asked to sign in before saving", async ({ page }) => {
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByRole("button", { name: `Save ${PRODUCT} to wishlist` }).click();
  await expect(page).toHaveURL(/\/login\?next=%2Fshop%2Flinen-weekend-dress$/);
});

test("save from the product page, see it in the account, remove it from a card", async ({ page }) => {
  await registerInBrowser(page, uniqueEmail("wish"));
  await page.goto(`${STORE}/shop/${SLUG}`);

  const heart = page.getByRole("button", { name: `Save ${PRODUCT} to wishlist` }).first();
  await heart.click();
  await expect(page.getByRole("button", { name: `Remove ${PRODUCT} from wishlist` }).first()).toHaveAttribute("aria-pressed", "true");

  await page.goto(`${STORE}/shop?search=Linen`);
  await expect(page.locator("article").filter({ hasText: PRODUCT }).getByRole("button", { name: `Remove ${PRODUCT} from wishlist` })).toBeVisible();

  await page.goto(`${STORE}/account/wishlist`);
  const card = page.locator("article").filter({ hasText: PRODUCT });
  await expect(card).toBeVisible();
  await card.getByRole("button", { name: `Remove ${PRODUCT} from wishlist` }).click();
  await expect(card.getByRole("button", { name: `Save ${PRODUCT} to wishlist` })).toBeVisible();

  await page.reload();
  await expect(page.getByText("Nothing saved yet.")).toBeVisible();
});
