import { describe, expect, it } from "vitest";
import { formatPrice } from "./money";

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
