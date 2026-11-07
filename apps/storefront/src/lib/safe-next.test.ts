import { describe, expect, it } from "vitest";
import { safeNext } from "./safe-next";

describe("safeNext", () => {
  it("follows paths on this site", () => {
    expect(safeNext("/shop/boxy-tee")).toBe("/shop/boxy-tee");
    expect(safeNext("/cart?x=1")).toBe("/cart?x=1");
  });

  it("falls back when there is nothing", () => {
    expect(safeNext(null)).toBe("/account");
    expect(safeNext("")).toBe("/account");
  });

  it("refuses links to other sites", () => {
    for (const bad of ["https://evil.example", "//evil.example", "/\\evil.example", "javascript:alert(1)"]) {
      expect(safeNext(bad)).toBe("/account");
    }
  });
});
