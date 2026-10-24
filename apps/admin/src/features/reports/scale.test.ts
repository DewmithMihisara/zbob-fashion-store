import { describe, expect, it } from "vitest";
import { compactMoney, compactNumber, niceTicks } from "./scale";

describe("niceTicks", () => {
  it("rounds to clean steps that cover the largest value", () => {
    expect(niceTicks(7300)).toEqual([0, 2000, 4000, 6000, 8000]);
    expect(niceTicks(9)).toEqual([0, 2.5, 5, 7.5, 10]);
  });

  it("still draws an axis when everything is zero", () => {
    expect(niceTicks(0)).toEqual([0, 1]);
  });

  it("ends exactly on a value that is already round", () => {
    expect(niceTicks(400)).toEqual([0, 100, 200, 300, 400]);
  });
});

describe("compactNumber", () => {
  it("writes small numbers in full and large ones compactly", () => {
    expect(compactNumber(4200)).toBe("4,200");
    expect(compactNumber(12500)).toBe("12.5K");
    expect(compactNumber(1_500_000)).toBe("1.5M");
  });

  it("formats money from cents", () => {
    expect(compactMoney(1_250_000)).toBe("$12.5K");
    expect(compactMoney(99)).toBe("$1");
  });
});
