"use client";

import type { PublicVariantOption } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useCart } from "@/components/cart/CartProvider";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { colors, find, firstAvailable, sortedSizes } from "@/lib/variant-options";
import { BackInStockForm } from "./BackInStockForm";
import { ColourSwatch } from "./ColourSwatch";
import { OrderNowButton } from "./OrderNowButton";

function OptionPill({
  label,
  active,
  soldOut,
  disabled = false,
  swatch,
  onClick,
}: {
  label: string;
  active: boolean;
  soldOut: boolean;
  disabled?: boolean;
  swatch?: string | null;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      aria-pressed={active}
      aria-label={soldOut ? `${label}, sold out` : label}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex min-w-11 items-center justify-center gap-2 rounded-full px-[14px] py-[9px] text-[15px] font-medium transition-colors ${
        active ? "bg-ink text-paper" : "bg-paper text-ink hover:bg-tile"
      } ${soldOut && !active ? "text-faint line-through" : ""} disabled:cursor-not-allowed disabled:opacity-40`}
    >
      <ColourSwatch hex={swatch ?? null} />
      {label}
    </button>
  );
}

/** Size and colour picker, with Add to cart and Order Now. Sold out choices offer a back in stock email instead. */
export function ProductPurchase({ slug, options }: { slug: string; options: PublicVariantOption[] }) {
  const router = useRouter();
  const session = useCustomerSession();
  const { addItem } = useCart();
  const [selected, setSelected] = useState(() => firstAvailable(options) ?? options[0]);
  const [added, setAdded] = useState(false);
  const { run, busy, error } = useCustomerAction();

  function chooseSize(size: string) {
    setAdded(false);
    const sameColour = selected ? find(options, size, selected.color) : undefined;
    setSelected(sameColour?.inStock ? sameColour : (options.find((o) => o.size === size && o.inStock) ?? sameColour ?? options.find((o) => o.size === size)));
  }

  function chooseColour(color: string) {
    setAdded(false);
    const option = selected ? find(options, selected.size, color) : undefined;
    if (option) {
      setSelected(option);
    }
  }

  async function add() {
    if (!session) {
      router.push(`/login?next=${encodeURIComponent(`/shop/${slug}`)}`);
      return;
    }
    if (selected && (await run(() => addItem(selected.variantId, 1)))) {
      setAdded(true);
    }
  }

  function orderNow() {
    if (!selected) {
      return;
    }
    const checkout = `/checkout?variant=${selected.variantId}&qty=1`;
    router.push(session ? checkout : `/login?next=${encodeURIComponent(checkout)}`);
  }

  const available = Boolean(selected?.inStock);

  return (
    <div className="flex flex-col gap-3">
      <div role="group" aria-label="Size">
        <p className="mb-2 text-[15px] font-medium text-muted">Size</p>
        <div className="flex flex-wrap gap-2">
          {sortedSizes(options).map((size) => (
            <OptionPill
              key={size}
              label={size}
              active={selected?.size === size}
              soldOut={!options.some((o) => o.size === size && o.inStock)}
              onClick={() => chooseSize(size)}
            />
          ))}
        </div>
      </div>
      <div role="group" aria-label="Colour">
        <p className="mb-2 text-[15px] font-medium text-muted">Colour</p>
        <div className="flex flex-wrap gap-2">
          {colors(options).map((color) => {
            const option = selected ? find(options, selected.size, color) : undefined;
            return (
              <OptionPill
                key={color}
                label={color}
                swatch={options.find((o) => o.color === color)?.colorHex}
                active={selected?.color === color}
                soldOut={!option?.inStock}
                disabled={!option}
                onClick={() => chooseColour(color)}
              />
            );
          })}
        </div>
      </div>
      <ErrorNotice message={error} />
      {available ? (
        <>
          <button
            type="button"
            disabled={busy}
            onClick={add}
            className="h-[54px] w-full rounded-full bg-paper text-[18px] font-medium tracking-tight-2 text-ink transition-transform enabled:hover:scale-[1.01] enabled:active:scale-[0.99] disabled:opacity-60"
          >
            {busy ? "Adding..." : "Add to cart"}
          </button>
          {added && (
            <p role="status" className="text-center text-[15px] text-muted">
              Added to your cart.{" "}
              <Link href="/cart" className="text-ink underline">
                View cart
              </Link>
            </p>
          )}
        </>
      ) : (
        selected && <BackInStockForm key={selected.variantId} variantId={selected.variantId} label={`${selected.size} in ${selected.color}`} />
      )}
      <OrderNowButton disabled={!available} onOrder={orderNow} />
    </div>
  );
}
