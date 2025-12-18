import { beforeEach, describe, expect, it, vi } from "vitest";
import { clearCustomerSession, getCustomerSession, setCustomerSession, subscribeCustomerSession } from "./customer-session";

const session = { token: "abc", fullName: "Sam" };

beforeEach(() => {
  clearCustomerSession();
});

describe("customer session", () => {
  it("is empty until set", () => {
    expect(getCustomerSession()).toBeNull();
  });

  it("returns a stable object between reads", () => {
    setCustomerSession(session);
    expect(getCustomerSession()).toEqual(session);
    expect(getCustomerSession()).toBe(getCustomerSession());
  });

  it("notifies listeners until they unsubscribe", () => {
    const listener = vi.fn();
    const unsubscribe = subscribeCustomerSession(listener);
    setCustomerSession(session);
    expect(listener).toHaveBeenCalledTimes(1);
    unsubscribe();
    clearCustomerSession();
    expect(listener).toHaveBeenCalledTimes(1);
  });

  it("hears changes from other tabs", () => {
    const listener = vi.fn();
    const unsubscribe = subscribeCustomerSession(listener);
    window.dispatchEvent(new StorageEvent("storage"));
    expect(listener).toHaveBeenCalledTimes(1);
    unsubscribe();
  });
});
