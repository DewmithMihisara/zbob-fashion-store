import { describe, expect, it } from "vitest";
import { photoLimitProblem } from "./product-photos";

describe("photoLimitProblem", () => {
  it("allows up to five photos", () => {
    expect(photoLimitProblem(0, 5)).toBeNull();
    expect(photoLimitProblem(4, 1)).toBeNull();
  });

  it("refuses a selection that would pass five and says how many fit", () => {
    expect(photoLimitProblem(3, 3)).toContain("You can add 2 more");
    expect(photoLimitProblem(0, 6)).toContain("You can add 5 more");
  });
});
