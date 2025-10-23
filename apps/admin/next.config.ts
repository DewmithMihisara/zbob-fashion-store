import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  agentRules: false,
  transpilePackages: ["@zbob/shared-types", "@zbob/shared-utils"],
};

export default nextConfig;
