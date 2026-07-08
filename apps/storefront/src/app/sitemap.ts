import type { MetadataRoute } from "next";
import { getBlogPosts, getProducts } from "@/lib/api";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

const PAGE_SIZE = 100;
const MAX_PAGES = 50;

/** Lists every published product and post. If the API is down it still returns the fixed pages. */
export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const pages: MetadataRoute.Sitemap = ["", "/shop", "/about", "/blog", "/contact"].map((path) => ({
    url: `${siteUrl}${path}`,
    changeFrequency: path === "" || path === "/shop" ? "daily" : "weekly",
    priority: path === "" ? 1 : 0.8,
  }));

  try {
    for (let page = 0; page < MAX_PAGES; page++) {
      const products = await getProducts({ size: PAGE_SIZE, page });
      for (const product of products.content) {
        pages.push({ url: `${siteUrl}/shop/${product.slug}`, changeFrequency: "weekly", priority: 0.7 });
      }
      if (page + 1 >= products.totalPages) break;
    }
    for (let page = 0; page < MAX_PAGES; page++) {
      const posts = await getBlogPosts({ size: PAGE_SIZE, page });
      for (const post of posts.content) {
        pages.push({
          url: `${siteUrl}/blog/${post.slug}`,
          lastModified: post.publishedAt ?? undefined,
          changeFrequency: "monthly",
          priority: 0.5,
        });
      }
      if (page + 1 >= posts.totalPages) break;
    }
  } catch {
    // Search engines retry later; serving the fixed pages is better than an error.
  }
  return pages;
}
