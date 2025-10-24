import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api, ApiRequestError } from "./api";
import { clearSession, getSession, setSession } from "./session";

function respond(status: number, body?: unknown) {
  return vi.fn().mockResolvedValue(new Response(body === undefined ? null : JSON.stringify(body), { status }));
}

beforeEach(() => {
  clearSession();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("api", () => {
  it("sends the bearer token and json body", async () => {
    setSession({ token: "abc", role: "ADMIN", fullName: "Sam" });
    const fetchMock = respond(200, { ok: true });
    vi.stubGlobal("fetch", fetchMock);

    await api("/thing", { method: "POST", json: { a: 1 } });

    const init = fetchMock.mock.calls[0]![1];
    expect(init.headers.Authorization).toBe("Bearer abc");
    expect(init.headers["Content-Type"]).toBe("application/json");
    expect(init.body).toBe('{"a":1}');
  });

  it("lets the browser set the content type for uploads", async () => {
    const fetchMock = respond(201, {});
    vi.stubGlobal("fetch", fetchMock);

    await api("/upload", { method: "POST", form: new FormData() });

    expect(fetchMock.mock.calls[0]![1].headers["Content-Type"]).toBeUndefined();
  });

  it("returns nothing for 204", async () => {
    vi.stubGlobal("fetch", respond(204));
    await expect(api("/thing", { method: "DELETE" })).resolves.toBeUndefined();
  });

  it("raises the server message and field errors", async () => {
    vi.stubGlobal("fetch", respond(400, { status: 400, code: "X", message: "Bad", fieldErrors: { name: "Required" } }));

    const error = await api("/thing").catch((e) => e);

    expect(error).toBeInstanceOf(ApiRequestError);
    expect(error.message).toBe("Bad");
    expect(error.fieldErrors).toEqual({ name: "Required" });
  });

  it("ends the session when a token is rejected", async () => {
    setSession({ token: "old", role: "STAFF", fullName: "Sam" });
    vi.stubGlobal("fetch", respond(401, { message: "Authentication required" }));

    await expect(api("/thing")).rejects.toThrow();

    expect(getSession()).toBeNull();
  });

  it("explains network failures", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new TypeError("failed")));

    await expect(api("/thing")).rejects.toThrow("Could not reach the server");
  });
});
