"use client";

import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { HeartIcon } from "@/components/ui/icons";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { useWishlist } from "./WishlistProvider";

export function WishlistButton({ productId, productName, className = "" }: { productId: number; productName: string; className?: string }) {
  const router = useRouter();
  const pathname = usePathname();
  const session = useCustomerSession();
  const { has, toggle } = useWishlist();
  const [busy, setBusy] = useState(false);
  const saved = has(productId);

  async function click() {
    if (!session) {
      router.push(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }
    setBusy(true);
    try {
      await toggle(productId);
    } catch {
      // the heart stays as it was, which shows the change did not happen
    } finally {
      setBusy(false);
    }
  }

  return (
    <button
      type="button"
      onClick={click}
      disabled={busy}
      aria-pressed={saved}
      aria-label={saved ? `Remove ${productName} from wishlist` : `Save ${productName} to wishlist`}
      className={`flex size-10 items-center justify-center rounded-full bg-paper transition-transform hover:scale-105 disabled:opacity-60 ${saved ? "text-accent" : "text-ink"} ${className}`}
    >
      <HeartIcon size={18} filled={saved} />
    </button>
  );
}
