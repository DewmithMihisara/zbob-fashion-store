import { describe, expect, it } from "vitest";
import { centsToInput, formatPrice, parsePrice } from "./money";

describe("formatPrice", () => {
  it("formats cents as dollars", () => {
    expect(formatPrice(3500)).toBe("$35.00");
  });

  it("keeps cents and thousands separators", () => {
    expect(formatPrice(123456)).toBe("$1,234.56");
  });

  it("formats zero", () => {
    expect(formatPrice(0)).toBe("$0.00");
  });
});

describe("parsePrice", () => {
  it("reads whole and decimal amounts without rounding errors", () => {
    expect(parsePrice("35")).toBe(3500);
    expect(parsePrice("35.5")).toBe(3550);
    expect(parsePrice("19.99")).toBe(1999);
    expect(parsePrice("1.10")).toBe(110);
  });

  it("trims spaces", () => {
    expect(parsePrice(" 12.00 ")).toBe(1200);
  });

  it("rejects anything that is not a plain price", () => {
    for (const bad of ["", "abc", "-5", "1.234", "1,000", "$5"]) {
      expect(parsePrice(bad)).toBeNull();
    }
  });
});

describe("centsToInput", () => {
  it("round trips with parsePrice", () => {
    expect(parsePrice(centsToInput(1999))).toBe(1999);
  });

  it("is empty when there is no price", () => {
    expect(centsToInput(null)).toBe("");
  });
});
