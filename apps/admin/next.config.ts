import type { NextConfig } from "next";
import path from "node:path";

const nextConfig: NextConfig = {
  agentRules: false,
  output: "standalone",
  outputFileTracingRoot: path.join(__dirname, "../.."),
  devIndicators: { position: "bottom-right" },
  transpilePackages: ["@zbob/shared-types", "@zbob/shared-utils"],
};

export default nextConfig;
