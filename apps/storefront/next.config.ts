import type { NextConfig } from "next";
import path from "node:path";

const nextConfig: NextConfig = {
  agentRules: false,
  output: "standalone",
  outputFileTracingRoot: path.join(__dirname, "../.."),
  transpilePackages: ["@zbob/shared-types", "@zbob/shared-utils"],
  images: {
    remotePatterns: [
      { protocol: "http", hostname: "localhost", pathname: "/uploads/**" },
      { protocol: "https", hostname: "storage.googleapis.com" },
    ],
    // Uploads are served from localhost only in development and in local test builds.
    dangerouslyAllowLocalIP: process.env.NODE_ENV !== "production" || process.env.ALLOW_LOCAL_IMAGES === "true",
  },
};

export default nextConfig;
