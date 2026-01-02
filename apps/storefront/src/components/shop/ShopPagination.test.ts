import { describe, expect, it } from "vitest";
import { pageNumbers } from "./ShopPagination";

describe("pageNumbers", () => {
  it("lists every page when there are few", () => {
    expect(pageNumbers(2, 3)).toEqual([1, 2, 3]);
  });

  it("shortens long runs with gaps", () => {
    expect(pageNumbers(5, 10)).toEqual([1, "gap", 4, 5, 6, "gap", 10]);
  });

  it("handles the first and last page", () => {
    expect(pageNumbers(1, 8)).toEqual([1, 2, "gap", 8]);
    expect(pageNumbers(8, 8)).toEqual([1, "gap", 7, 8]);
  });
});
