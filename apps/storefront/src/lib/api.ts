import type {
  BlogPost,
  BlogPostSummary,
  CategoryWithPrices,
  Page,
  ProductBadge,
  ProductSummary,
  PublicProduct,
  ReviewSummary,
  SiteMedia,
} from "@zbob/shared-types";

const API_URL = process.env.API_URL ?? "http://localhost:8080/api/v1";

export class ApiError extends Error {
  constructor(readonly status: number, path: string) {
    super(`API ${status} for ${path}`);
  }
}

/** Requests are never cached, so changes in the admin dashboard show on the next page load. */
async function get<T>(path: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, { cache: "no-store" });
  if (!response.ok) {
    throw new ApiError(response.status, path);
  }
  return response.json() as Promise<T>;
}

/** Returns null for a 404 so pages can show their not-found screen. */
async function getOrNull<T>(path: string): Promise<T | null> {
  try {
    return await get<T>(path);
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}

function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") {
      search.set(key, String(value));
    }
  }
  const text = search.toString();
  return text ? `?${text}` : "";
}

export function getProducts(options: { category?: string; badge?: ProductBadge; size?: number } = {}) {
  return get<Page<ProductSummary>>(`/products${query(options)}`);
}

export function getProduct(slug: string) {
  return getOrNull<PublicProduct>(`/products/${encodeURIComponent(slug)}`);
}

export function getCategories() {
  return get<CategoryWithPrices[]>("/categories/with-prices");
}

export function getReviews() {
  return get<ReviewSummary>("/reviews");
}

export function getBlogPosts(options: { topic?: string; size?: number } = {}) {
  return get<Page<BlogPostSummary>>(`/blog${query(options)}`);
}

export function getBlogPost(slug: string) {
  return getOrNull<BlogPost>(`/blog/${encodeURIComponent(slug)}`);
}

export function getSiteMedia() {
  return get<SiteMedia>("/site-media");
}
