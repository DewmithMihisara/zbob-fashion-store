export interface BlogPostSummary {
  id: number;
  title: string;
  slug: string;
  excerpt: string;
  topic: string;
  readMinutes: number;
  coverUrl: string | null;
  publishedAt: string | null;
}

export interface BlogPost extends BlogPostSummary {
  body: string;
}

export interface Review {
  id: number;
  authorName: string;
  authorTitle: string | null;
  headline: string;
  body: string;
  rating: number;
  approved: boolean;
  productId: number | null;
  productName: string | null;
  createdAt: string;
}

export interface PublicReview {
  id: number;
  authorName: string;
  headline: string;
  body: string;
  rating: number;
  createdAt: string;
}

export interface ProductReviews {
  averageRating: number;
  reviewCount: number;
  reviews: import("./api").Page<PublicReview>;
}

export interface ReviewSummary {
  averageRating: number;
  reviewCount: number;
  reviews: Review[];
}

/** Storefront photo slots: slot name to URL. Empty slots are absent. */
export type SiteMedia = Record<string, string>;

export interface SiteMediaSlot {
  slot: string;
  label: string;
  url: string | null;
}
