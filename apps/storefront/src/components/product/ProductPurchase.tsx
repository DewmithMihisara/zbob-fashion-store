"use client";

import type { PublicVariantOption } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useCart } from "@/components/cart/CartProvider";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { chooseSize, colors, find, firstAvailable, sortedSizes } from "@/lib/variant-options";

function OptionPill({ label, active, disabled, onClick }: { label: string; active: boolean; disabled: boolean; onClick: () => void }) {
  return (
    <button
      type="button"
      aria-pressed={active}
      disabled={disabled}
      onClick={onClick}
      className={`min-w-11 rounded-full px-[14px] py-[9px] text-[15px] font-medium transition-colors ${
        active ? "bg-ink text-paper" : "bg-paper text-ink hover:bg-tile"
      } disabled:cursor-not-allowed disabled:bg-transparent disabled:text-faint disabled:line-through`}
    >
      {label}
    </button>
  );
}

export function ProductPurchase({ slug, options }: { slug: string; options: PublicVariantOption[] }) {
  const router = useRouter();
  const session = useCustomerSession();
  const { addItem } = useCart();
  const [selected, setSelected] = useState(() => firstAvailable(options));
  const [added, setAdded] = useState(false);
  const { run, busy, error } = useCustomerAction();

  function choose(option: PublicVariantOption | undefined) {
    setAdded(false);
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
              disabled={!options.some((o) => o.size === size && o.inStock)}
              onClick={() => choose(chooseSize(options, size, selected?.color))}
            />
          ))}
        </div>
      </div>
      <div role="group" aria-label="Colour">
        <p className="mb-2 text-[15px] font-medium text-muted">Colour</p>
        <div className="flex flex-wrap gap-2">
          {colors(options).map((color) => (
            <OptionPill
              key={color}
              label={color}
              active={selected?.color === color}
              disabled={!selected || !find(options, selected.size, color)?.inStock}
              onClick={() => choose(selected && find(options, selected.size, color))}
            />
          ))}
        </div>
      </div>
      <ErrorNotice message={error} />
      <button
        type="button"
        disabled={busy || !selected}
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
    </div>
  );
}
