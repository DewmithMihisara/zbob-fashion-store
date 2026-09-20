import { describe, expect, it } from "vitest";
import { parseSizeGuide } from "./size-guide";

describe("parseSizeGuide", () => {
  it("turns piped lines into a table and keeps notes as text", () => {
    expect(parseSizeGuide("Measured flat.\nSize | Chest | Length\nS | 90 cm | 68 cm\nM | 96 cm | 70 cm\n\nBetween sizes? Go up.")).toEqual([
      { type: "text", text: "Measured flat." },
      { type: "table", rows: [["Size", "Chest", "Length"], ["S", "90 cm", "68 cm"], ["M", "96 cm", "70 cm"]] },
      { type: "text", text: "Between sizes? Go up." },
    ]);
  });

  it("keeps markup as text", () => {
    expect(parseSizeGuide("<b>bold</b>")).toEqual([{ type: "text", text: "<b>bold</b>" }]);
  });

  it("is empty for blank text", () => {
    expect(parseSizeGuide(" \n ")).toEqual([]);
  });
});
