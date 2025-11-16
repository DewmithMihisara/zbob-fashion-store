import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  agentRules: false,
  devIndicators: { position: "bottom-right" },
  transpilePackages: ["@zbob/shared-types", "@zbob/shared-utils"],
};

export default nextConfig;
