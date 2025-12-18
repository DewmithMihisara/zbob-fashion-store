"use client";

import { useSyncExternalStore } from "react";
import { getCustomerSession, subscribeCustomerSession } from "@/lib/customer-session";

export function useCustomerSession() {
  return useSyncExternalStore(subscribeCustomerSession, getCustomerSession, () => null);
}

/** False during server rendering, when the saved login can not be read yet. */
export function useIsClient() {
  return useSyncExternalStore(
    () => () => {},
    () => true,
    () => false,
  );
}
