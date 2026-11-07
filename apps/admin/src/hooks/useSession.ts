"use client";

import { useSyncExternalStore } from "react";
import { getSession, subscribeSession } from "@/lib/session";

export function useSession() {
  return useSyncExternalStore(subscribeSession, getSession, () => null);
}

/** False during server rendering, when session storage can not be read yet. */
export function useIsClient() {
  return useSyncExternalStore(
    () => () => {},
    () => true,
    () => false,
  );
}
