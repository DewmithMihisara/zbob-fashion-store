import type { BlogPost, ProductReviews, PublicProduct } from "@zbob/shared-types";
import { siteUrl } from "./site";

type Json = Record<string, unknown>;

function price(cents: number): string {
  return (cents / 100).toFixed(2);
}

/** Search engines read this to show price, availability and rating with the product. */
export function productJsonLd(product: PublicProduct, reviews: ProductReviews | null): Json {
  const data: Json = {
    "@context": "https://schema.org",
    "@type": "Product",
    name: product.name,
    description: product.description,
    url: `${siteUrl}/shop/${product.slug}`,
    image: product.images.map((image) => image.url),
    category: product.category.name,
    brand: { "@type": "Brand", name: "zbob" },
    offers: {
      "@type": "Offer",
      price: price(product.priceCents),
      priceCurrency: "USD",
      availability: product.inStock ? "https://schema.org/InStock" : "https://schema.org/OutOfStock",
      url: `${siteUrl}/shop/${product.slug}`,
    },
  };
  if (reviews && reviews.reviewCount > 0) {
    data.aggregateRating = { "@type": "AggregateRating", ratingValue: reviews.averageRating, reviewCount: reviews.reviewCount };
  }
  return data;
}

export function articleJsonLd(post: BlogPost): Json {
  return {
    "@context": "https://schema.org",
    "@type": "BlogPosting",
    headline: post.title,
    description: post.excerpt,
    url: `${siteUrl}/blog/${post.slug}`,
    datePublished: post.publishedAt ?? undefined,
    image: post.coverUrl ?? undefined,
    author: { "@type": "Organization", name: "zbob" },
    publisher: { "@type": "Organization", name: "zbob" },
  };
}

export function organizationJsonLd(): Json {
  return { "@context": "https://schema.org", "@type": "Organization", name: "zbob", url: siteUrl };
}

/** Escapes "<" so text such as a product name can never close the script tag it sits in. */
export function serializeJsonLd(data: Json): string {
  return JSON.stringify(data).replace(/</g, "\\u003c");
}
