import { expect, test } from "@playwright/test";
import { API, STORE } from "./helpers";
import { adminToken, registerInBrowser, uniqueEmail } from "./customer-helpers";

const SLUG = "harbour-camp-shirt";

test("a review appears once staff approve it, and counts in the rating", async ({ page, request }) => {
  const headline = `Great shirt ${Date.now()}`;
  await page.goto(`${STORE}/shop/${SLUG}`);
  await expect(page.getByText("Sign in to write a review.", { exact: false })).toBeVisible();

  await registerInBrowser(page, uniqueEmail("reviewer"), "Kasun Silva");
  await page.goto(`${STORE}/shop/${SLUG}`);
  await page.getByLabel("Rating").selectOption("4");
  await page.getByLabel("Headline").fill(headline);
  await page.getByLabel("Your review").fill("Light and easy to wear on hot days.");
  await page.getByRole("button", { name: "Send review" }).click();
  await expect(page.getByText("Thanks for your review.")).toBeVisible();
  await expect(page.getByText(headline)).toHaveCount(0);

  const token = await adminToken(request);
  const headers = { Authorization: `Bearer ${token}` };
  const waiting = (await (await request.get(`${API}/admin/reviews?approved=false&size=100`, { headers })).json()) as {
    content: { id: number; headline: string; authorName: string; authorTitle: string | null; body: string; rating: number }[];
  };
  const review = waiting.content.find((r) => r.headline === headline)!;
  expect(review).toBeTruthy();
  await request.put(`${API}/admin/reviews/${review.id}`, { headers, data: { ...review, approved: true } });

  await page.reload();
  const card = page.getByRole("listitem").filter({ hasText: headline });
  await expect(card).toContainText("Kasun Silva");
  await expect(page.getByText(/\/5 from \d+ reviews?/)).toBeVisible();
  const jsonLd = await page.locator('script[type="application/ld+json"]').first().textContent();
  expect(JSON.parse(jsonLd!).aggregateRating.reviewCount).toBeGreaterThan(0);

  await page.getByLabel("Headline").fill("Again");
  await page.getByLabel("Your review").fill("Second try");
  await page.getByRole("button", { name: "Send review" }).click();
  await expect(page.getByTestId("form-error")).toHaveText("You have already reviewed this product");
});
