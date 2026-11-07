"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { customerApi } from "@/lib/customer-api";

interface WishlistContextValue {
  has: (productId: number) => boolean;
  toggle: (productId: number) => Promise<void>;
}

const WishlistContext = createContext<WishlistContextValue | null>(null);

/** Mirrors which products the signed in customer has saved, so every heart on the page agrees. */
export function WishlistProvider({ children }: { children: ReactNode }) {
  const token = useCustomerSession()?.token ?? null;
  const [loaded, setLoaded] = useState<{ token: string; ids: Set<number> } | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }
    let current = true;
    customerApi<number[]>("/me/wishlist/ids").then(
      (ids) => {
        if (current) setLoaded({ token, ids: new Set(ids) });
      },
      () => {
        if (current) setLoaded({ token, ids: new Set() });
      },
    );
    return () => {
      current = false;
    };
  }, [token]);

  const ids = useMemo(() => (token && loaded?.token === token ? loaded.ids : new Set<number>()), [token, loaded]);

  const toggle = useCallback(
    async (productId: number) => {
      if (!token) {
        return;
      }
      const saved = ids.has(productId);
      await customerApi(`/me/wishlist/${productId}`, { method: saved ? "DELETE" : "PUT" });
      const next = new Set(ids);
      if (saved) {
        next.delete(productId);
      } else {
        next.add(productId);
      }
      setLoaded({ token, ids: next });
    },
    [token, ids],
  );

  const value = useMemo(() => ({ has: (id: number) => ids.has(id), toggle }), [ids, toggle]);
  return <WishlistContext.Provider value={value}>{children}</WishlistContext.Provider>;
}

export function useWishlist(): WishlistContextValue {
  const value = useContext(WishlistContext);
  if (!value) {
    throw new Error("useWishlist must be used inside WishlistProvider");
  }
  return value;
}
