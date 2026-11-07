import { defineConfig } from "@playwright/test";

/** Expects the API, storefront and admin to be running already. See README.md in this folder. */
export default defineConfig({
  testDir: "./specs",
  globalSetup: "./global-setup.ts",
  workers: 1, // the specs change shared data such as stock
  retries: 0,
  reporter: [["list"]],
  timeout: 60_000,
  use: {
    trace: "retain-on-failure",
  },
});
