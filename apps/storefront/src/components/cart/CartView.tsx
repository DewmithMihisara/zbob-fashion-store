"use client";

import type { CartItem } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { LinkButton } from "@/components/ui/Button";
import { ErrorNotice } from "@/components/ui/FormField";
import { Photo } from "@/components/ui/Photo";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerSession, useIsClient } from "@/hooks/useCustomerSession";
import { useCart } from "./CartProvider";

const MAX_QUANTITY = 99;

function CartLine({ item }: { item: CartItem }) {
  const { updateQuantity, removeItem } = useCart();
  const { run, busy, error } = useCustomerAction();
  const stepClass = "flex size-9 items-center justify-center rounded-full bg-paper text-[18px] hover:bg-line disabled:opacity-40";

  return (
    <li className="rounded-card bg-tile p-3">
      <div className="flex gap-4">
        <Link href={`/shop/${item.productSlug}`} className="relative block size-[110px] shrink-0 overflow-hidden rounded-[10px] bg-line">
          <Photo src={item.imageUrl} alt={item.productName} sizes="110px" />
        </Link>
        <div className="flex min-w-0 flex-1 flex-col">
          <div className="flex flex-wrap justify-between gap-x-4">
            <Link href={`/shop/${item.productSlug}`} className="text-[18px] font-medium tracking-tight-2">
              {item.productName}
            </Link>
            <span className="text-[18px] font-medium tracking-tight-2">{formatPrice(item.lineTotalCents)}</span>
          </div>
          <p className="text-[15px] text-muted">
            {item.size} / {item.color} · {formatPrice(item.unitPriceCents)} each
          </p>
          {!item.available && <p className="mt-1 text-[14px] text-red-700">Not enough stock for this quantity. Lower it or remove this item.</p>}
          <div className="mt-auto flex flex-wrap items-center justify-between gap-2 pt-3">
            <div className="flex items-center gap-2" role="group" aria-label={`Quantity of ${item.productName}`}>
              <button type="button" aria-label="Decrease quantity" disabled={busy || item.quantity <= 1} onClick={() => run(() => updateQuantity(item.id, item.quantity - 1))} className={stepClass}>
                -
              </button>
              <span className="min-w-6 text-center text-[16px] font-medium" data-testid="quantity">
                {item.quantity}
              </span>
              <button type="button" aria-label="Increase quantity" disabled={busy || item.quantity >= MAX_QUANTITY} onClick={() => run(() => updateQuantity(item.id, item.quantity + 1))} className={stepClass}>
                +
              </button>
            </div>
            <button type="button" disabled={busy} onClick={() => run(() => removeItem(item.id))} className="text-[15px] text-muted underline hover:text-ink">
              Remove
            </button>
          </div>
        </div>
      </div>
      {error && (
        <div className="mt-3">
          <ErrorNotice message={error} />
        </div>
      )}
    </li>
  );
}

export function CartView() {
  const isClient = useIsClient();
  const session = useCustomerSession();
  const { cart, loadError } = useCart();

  if (!isClient) {
    return null;
  }
  if (!session) {
    return (
      <div className="flex flex-col items-center gap-4 py-16 text-center">
        <p className="text-[18px] text-muted">Sign in to see your cart. It stays saved on your account.</p>
        <LinkButton href="/login?next=%2Fcart">Sign in</LinkButton>
      </div>
    );
  }
  if (loadError) {
    return <ErrorNotice message={loadError} />;
  }
  if (!cart) {
    return <p className="py-16 text-center text-muted">Loading your cart...</p>;
  }
  if (cart.items.length === 0) {
    return (
      <div className="flex flex-col items-center gap-4 py-16 text-center">
        <p className="text-[18px] text-muted">Your cart is empty.</p>
        <LinkButton href="/shop">Shop all items</LinkButton>
      </div>
    );
  }
  return (
    <div className="grid items-start gap-5 tablet:grid-cols-[1fr_340px]">
      <ul className="flex flex-col gap-3">
        {cart.items.map((item) => (
          <CartLine key={item.id} item={item} />
        ))}
      </ul>
      <aside className="rounded-card bg-tile p-5">
        <h2 className="text-[22px] font-medium tracking-tight-3">Summary</h2>
        <p className="mt-4 flex justify-between text-[17px]">
          <span>Subtotal</span>
          <span className="font-medium" data-testid="subtotal">
            {formatPrice(cart.subtotalCents)}
          </span>
        </p>
        <p className="mt-1 text-[14px] text-muted">Delivery and any discounts are worked out at checkout.</p>
        {/* No payment gateway has been chosen yet. Connect checkout here when one is. */}
        <button type="button" disabled className="mt-5 h-12 w-full rounded-full bg-faint text-[17px] font-medium text-paper">
          Checkout coming soon
        </button>
        <p className="mt-3 text-center text-[14px] text-muted">
          Online payment is not open yet.{" "}
          <Link href="/contact" className="text-ink underline">
            Contact us
          </Link>{" "}
          to order.
        </p>
      </aside>
    </div>
  );
}
