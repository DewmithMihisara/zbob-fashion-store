import { describe, expect, it } from "vitest";
import { parsePostBody } from "./post-body";

describe("parsePostBody", () => {
  it("recognises headings, paragraphs and lists", () => {
    expect(parsePostBody("## Title\n\nSome text\n\n- One\n- Two")).toEqual([
      { type: "heading", text: "Title" },
      { type: "paragraph", text: "Some text" },
      { type: "list", items: ["One", "Two"] },
    ]);
  });

  it("keeps markup as plain text", () => {
    expect(parsePostBody("<script>alert(1)</script>")).toEqual([
      { type: "paragraph", text: "<script>alert(1)</script>" },
    ]);
  });

  it("starts a new list after other content", () => {
    const blocks = parsePostBody("- A\nText\n- B");
    expect(blocks.map((block) => block.type)).toEqual(["list", "paragraph", "list"]);
  });

  it("returns nothing for empty text", () => {
    expect(parsePostBody("\n  \n")).toEqual([]);
  });
});
