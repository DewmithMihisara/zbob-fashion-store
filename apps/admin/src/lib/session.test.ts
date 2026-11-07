import { beforeEach, describe, expect, it, vi } from "vitest";
import { clearSession, getSession, setSession, subscribeSession } from "./session";

const session = { token: "abc", role: "ADMIN" as const, fullName: "Sam" };

beforeEach(() => {
  clearSession();
});

describe("session", () => {
  it("is empty until set", () => {
    expect(getSession()).toBeNull();
  });

  it("returns the same object between reads", () => {
    setSession(session);
    expect(getSession()).toEqual(session);
    expect(getSession()).toBe(getSession());
  });

  it("clears", () => {
    setSession(session);
    clearSession();
    expect(getSession()).toBeNull();
  });

  it("notifies and unsubscribes listeners", () => {
    const listener = vi.fn();
    const unsubscribe = subscribeSession(listener);
    setSession(session);
    expect(listener).toHaveBeenCalledTimes(1);
    unsubscribe();
    clearSession();
    expect(listener).toHaveBeenCalledTimes(1);
  });
});
