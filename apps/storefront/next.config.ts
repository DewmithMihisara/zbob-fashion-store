import type { NextConfig } from "next";
import path from "node:path";

const nextConfig: NextConfig = {
  agentRules: false,
  output: "standalone",
  outputFileTracingRoot: path.join(__dirname, "../.."),
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
