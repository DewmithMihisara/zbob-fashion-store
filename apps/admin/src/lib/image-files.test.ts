import { describe, expect, it } from "vitest";
import { imageFileProblem } from "./image-files";

describe("imageFileProblem", () => {
  it("accepts allowed images", () => {
    for (const type of ["image/jpeg", "image/png", "image/webp"]) {
      expect(imageFileProblem({ name: "a", type, size: 1000 })).toBeNull();
    }
  });

  it("rejects other file types", () => {
    expect(imageFileProblem({ name: "a.svg", type: "image/svg+xml", size: 10 })).toContain("only JPEG");
    expect(imageFileProblem({ name: "a.pdf", type: "application/pdf", size: 10 })).toContain("a.pdf");
  });

  it("rejects files over 10 MB", () => {
    expect(imageFileProblem({ name: "a.png", type: "image/png", size: 10 * 1024 * 1024 + 1 })).toContain("10 MB");
  });

  it("accepts a file of exactly 10 MB", () => {
    expect(imageFileProblem({ name: "a.png", type: "image/png", size: 10 * 1024 * 1024 })).toBeNull();
  });
});
