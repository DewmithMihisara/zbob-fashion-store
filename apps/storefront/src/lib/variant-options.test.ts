import { describe, expect, it } from "vitest";
import { chooseSize, colors, firstAvailable, sortedSizes } from "./variant-options";

const option = (variantId: number, size: string, color: string, inStock = true) => ({ variantId, size, color, inStock });

describe("sortedSizes", () => {
  it("orders clothing sizes naturally and removes duplicates", () => {
    const options = [option(1, "L", "Black"), option(2, "S", "Black"), option(3, "XL", "Black"), option(4, "S", "White")];
    expect(sortedSizes(options)).toEqual(["S", "L", "XL"]);
  });

  it("orders numeric sizes by number, after named sizes", () => {
    const options = [option(1, "10", "Black"), option(2, "8", "Black"), option(3, "M", "Black")];
    expect(sortedSizes(options)).toEqual(["M", "8", "10"]);
  });
});

describe("colors", () => {
  it("lists each colour once", () => {
    expect(colors([option(1, "S", "White"), option(2, "M", "Black"), option(3, "L", "White")])).toEqual(["Black", "White"]);
  });
});

describe("firstAvailable", () => {
  it("skips sizes that are out of stock", () => {
    expect(firstAvailable([option(1, "S", "Black", false), option(2, "M", "Black")])?.variantId).toBe(2);
  });

  it("is empty when nothing can be bought", () => {
    expect(firstAvailable([option(1, "S", "Black", false)])).toBeUndefined();
    expect(firstAvailable([])).toBeUndefined();
  });
});

describe("chooseSize", () => {
  const options = [option(1, "S", "Black"), option(2, "S", "White"), option(3, "M", "Black", false), option(4, "M", "White")];

  it("keeps the colour when that size has it", () => {
    expect(chooseSize(options, "S", "White")?.variantId).toBe(2);
  });

  it("changes colour when the current one is out of stock in that size", () => {
    expect(chooseSize(options, "M", "Black")?.variantId).toBe(4);
  });
});
