import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  transpilePackages: ["@zbob/shared-types", "@zbob/shared-utils"],
  images: {
    remotePatterns: [
      { protocol: "http", hostname: "localhost", port: "8080", pathname: "/uploads/**" },
      { protocol: "https", hostname: "storage.googleapis.com" },
    ],
    // Uploads are served from localhost in development only.
    dangerouslyAllowLocalIP: process.env.NODE_ENV !== "production",
  },
};

export default nextConfig;
