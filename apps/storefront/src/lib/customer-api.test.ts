import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { customerApi, CustomerApiError } from "./customer-api";
import { clearCustomerSession, getCustomerSession, setCustomerSession } from "./customer-session";

function respond(status: number, body?: unknown) {
  return vi.fn().mockResolvedValue(new Response(body === undefined ? null : JSON.stringify(body), { status }));
}

beforeEach(() => {
  clearCustomerSession();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("customerApi", () => {
  it("sends the bearer token when signed in", async () => {
    setCustomerSession({ token: "abc", fullName: "Sam" });
    const fetchMock = respond(200, {});
    vi.stubGlobal("fetch", fetchMock);

    await customerApi("/me");

    expect(fetchMock.mock.calls[0]![1].headers.Authorization).toBe("Bearer abc");
  });

  it("sends no token when signed out", async () => {
    const fetchMock = respond(200, {});
    vi.stubGlobal("fetch", fetchMock);

    await customerApi("/auth/customer/login", { method: "POST", json: {} });

    expect(fetchMock.mock.calls[0]![1].headers.Authorization).toBeUndefined();
  });

  it("raises the server message and field errors", async () => {
    vi.stubGlobal("fetch", respond(409, { message: "Only 2 left in stock", fieldErrors: { quantity: "Too many" } }));

    const error = await customerApi("/cart/items").catch((e) => e);

    expect(error).toBeInstanceOf(CustomerApiError);
    expect(error.message).toBe("Only 2 left in stock");
    expect(error.fieldErrors.quantity).toBe("Too many");
  });

  it("accepts an accepted answer with no body", async () => {
    vi.stubGlobal("fetch", respond(202));

    await expect(customerApi("/auth/customer/password-reset", { method: "POST", json: {} })).resolves.toBeUndefined();
  });

  it("signs out when the token is rejected", async () => {
    setCustomerSession({ token: "old", fullName: "Sam" });
    vi.stubGlobal("fetch", respond(401, { message: "Authentication required" }));

    await expect(customerApi("/me")).rejects.toThrow();

    expect(getCustomerSession()).toBeNull();
  });

  it("keeps the session when a wrong password is typed while signed out", async () => {
    vi.stubGlobal("fetch", respond(401, { message: "Invalid email or password" }));

    await expect(customerApi("/auth/customer/login", { method: "POST", json: {} })).rejects.toThrow("Invalid email or password");
  });
});
