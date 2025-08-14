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
