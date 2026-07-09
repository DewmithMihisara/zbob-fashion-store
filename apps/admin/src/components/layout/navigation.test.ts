import { describe, expect, it } from "vitest";
import { navigationFor } from "./navigation";

const labels = (role: "ADMIN" | "STAFF") => navigationFor(role).flatMap((group) => group.items.map((item) => item.label));

describe("navigationFor", () => {
  it("shows staff management to admins", () => {
    expect(labels("ADMIN")).toContain("Staff");
  });

  it("hides staff management and the activity log from staff", () => {
    expect(labels("STAFF")).not.toContain("Staff");
    expect(labels("STAFF")).not.toContain("Activity");
    expect(labels("ADMIN")).toContain("Activity");
  });

  it("always shows photos and stock", () => {
    for (const role of ["ADMIN", "STAFF"] as const) {
      expect(labels(role)).toEqual(expect.arrayContaining(["Products", "Stock", "Site photos"]));
    }
  });
});
