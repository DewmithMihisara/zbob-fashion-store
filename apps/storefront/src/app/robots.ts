import type { MetadataRoute } from "next";
import { siteUrl } from "@/lib/site";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: "/",
      disallow: ["/account", "/cart", "/checkout", "/login", "/register", "/forgot-password", "/reset-password"],
    },
    sitemap: `${siteUrl}/sitemap.xml`,
  };
}
