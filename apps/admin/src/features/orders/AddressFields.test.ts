import { describe, expect, it } from "vitest";
import { emptyAddress, toAddressJson } from "./AddressFields";

describe("toAddressJson", () => {
  it("sends empty optional fields as null", () => {
    const json = toAddressJson({ ...emptyAddress, recipient: "Sam" });
    expect(json.line2).toBeNull();
    expect(json.region).toBeNull();
    expect(json.phone).toBeNull();
    expect(json.recipient).toBe("Sam");
  });

  it("uppercases the country code", () => {
    expect(toAddressJson({ ...emptyAddress, country: "lk" }).country).toBe("LK");
  });
});
