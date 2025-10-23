import next from "eslint-config-next";

const config = [
  ...next,
  { ignores: [".next/**", "next-env.d.ts"] },
  // Photos here are small previews of uploads, so image optimization adds nothing.
  { rules: { "@next/next/no-img-element": "off" } },
];

export default config;
