export interface CustomerSession {
  token: string;
  fullName: string;
}

const KEY = "zbob-customer-session";

const listeners = new Set<() => void>();
let cachedRaw: string | null = null;
let cached: CustomerSession | null = null;

/** Parsed once per stored value, so React sees a stable object between renders. */
export function getCustomerSession(): CustomerSession | null {
  if (typeof window === "undefined") {
    return null;
  }
  const raw = window.localStorage.getItem(KEY);
  if (raw !== cachedRaw) {
    cachedRaw = raw;
    cached = raw ? (JSON.parse(raw) as CustomerSession) : null;
  }
  return cached;
}

export function setCustomerSession(session: CustomerSession): void {
  window.localStorage.setItem(KEY, JSON.stringify(session));
  listeners.forEach((listener) => listener());
}

export function clearCustomerSession(): void {
  window.localStorage.removeItem(KEY);
  listeners.forEach((listener) => listener());
}

/** Also listens for sign in and sign out in other tabs. */
export function subscribeCustomerSession(listener: () => void): () => void {
  listeners.add(listener);
  window.addEventListener("storage", listener);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", listener);
  };
}
