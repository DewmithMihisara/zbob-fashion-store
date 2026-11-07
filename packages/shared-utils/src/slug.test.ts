import { describe, expect, it } from "vitest";
import { slugify } from "./slug";

describe("slugify", () => {
  it("lowercases and hyphenates", () => {
    expect(slugify("Classic Boxy Tee")).toBe("classic-boxy-tee");
  });

  it("drops apostrophes", () => {
    expect(slugify("Men's Wear")).toBe("mens-wear");
  });

  it("strips accents and trims separators", () => {
    expect(slugify("  Café -- Noir! ")).toBe("cafe-noir");
  });
});
