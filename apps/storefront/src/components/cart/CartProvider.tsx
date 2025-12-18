"use client";

import type { Cart } from "@zbob/shared-types";
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { customerApi, customerErrorMessage } from "@/lib/customer-api";

interface CartContextValue {
  /** Null while loading and when signed out. */
  cart: Cart | null;
  count: number;
  loadError: string | null;
  addItem: (variantId: number, quantity: number) => Promise<void>;
  updateQuantity: (itemId: number, quantity: number) => Promise<void>;
  removeItem: (itemId: number) => Promise<void>;
}

const CartContext = createContext<CartContextValue | null>(null);

interface Loaded {
  token: string;
  cart?: Cart;
  error?: string;
}

/** The cart belongs to the account on the server. This only mirrors it, so the header count stays current. */
export function CartProvider({ children }: { children: ReactNode }) {
  const token = useCustomerSession()?.token ?? null;
  const [loaded, setLoaded] = useState<Loaded | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }
    let current = true;
    customerApi<Cart>("/cart").then(
      (cart) => {
        if (current) setLoaded({ token, cart });
      },
      (error: unknown) => {
        if (current) setLoaded({ token, error: customerErrorMessage(error) });
      },
    );
    return () => {
      current = false;
    };
  }, [token]);

  const apply = useCallback(
    async (path: string, options: Parameters<typeof customerApi>[1]) => {
      const cart = await customerApi<Cart>(path, options);
      if (token) {
        setLoaded({ token, cart });
      }
    },
    [token],
  );

  const value = useMemo<CartContextValue>(() => {
    // Ignoring state from an earlier login means one customer's cart never shows for another.
    const current = token && loaded?.token === token ? loaded : null;
    const cart = current?.cart ?? null;
    return {
      cart,
      count: cart ? cart.items.reduce((total, item) => total + item.quantity, 0) : 0,
      loadError: current?.error ?? null,
      addItem: (variantId, quantity) => apply("/cart/items", { method: "POST", json: { variantId, quantity } }),
      updateQuantity: (itemId, quantity) => apply(`/cart/items/${itemId}`, { method: "PATCH", json: { quantity } }),
      removeItem: (itemId) => apply(`/cart/items/${itemId}`, { method: "DELETE" }),
    };
  }, [token, loaded, apply]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
  const value = useContext(CartContext);
  if (!value) {
    throw new Error("useCart must be used inside CartProvider");
  }
  return value;
}
