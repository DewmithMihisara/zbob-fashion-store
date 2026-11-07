import type { Role } from "@zbob/shared-types";

export interface Session {
  token: string;
  role: Role;
  fullName: string;
}

const KEY = "zbob-admin-session";

const listeners = new Set<() => void>();
let cachedRaw: string | null = null;
let cached: Session | null = null;

/** Parsed once per stored value, so React sees a stable object between renders. */
export function getSession(): Session | null {
  if (typeof window === "undefined") {
    return null;
  }
  const raw = window.sessionStorage.getItem(KEY);
  if (raw !== cachedRaw) {
    cachedRaw = raw;
    cached = raw ? (JSON.parse(raw) as Session) : null;
  }
  return cached;
}

export function setSession(session: Session): void {
  window.sessionStorage.setItem(KEY, JSON.stringify(session));
  listeners.forEach((listener) => listener());
}

export function clearSession(): void {
  window.sessionStorage.removeItem(KEY);
  listeners.forEach((listener) => listener());
}

export function subscribeSession(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
