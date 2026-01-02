"use client";

import type { ProductSummary } from "@zbob/shared-types";
import Image from "next/image";
import Link from "next/link";
import { useState } from "react";
import { Photo } from "@/components/ui/Photo";
import { WishlistButton } from "@/components/wishlist/WishlistButton";
import { Badge } from "./Badge";
import { Price } from "./Price";

export function ProductCard({ product }: { product: ProductSummary }) {
  const [active, setActive] = useState(0);
  const imageUrl = product.imageUrls[active] ?? product.imageUrl;

  return (
    <article>
      <div className="relative">
      <Link href={`/shop/${product.slug}`} className="group relative block aspect-[373/420] overflow-hidden rounded-card bg-tile">
        <Photo
          src={imageUrl}
          alt={product.name}
          sizes="(min-width: 810px) 33vw, 100vw"
          className="object-cover transition-transform duration-500 group-hover:scale-105"
        />
        {product.badge && (
          <span className="absolute top-[14px] left-[14px]">
            <Badge badge={product.badge} />
          </span>
        )}
        {!product.inStock && (
          <span className="absolute top-[14px] right-[14px] rounded-full bg-paper px-[11px] py-[6px] text-[14px] text-muted">
            Out of stock
          </span>
        )}
      </Link>
        <WishlistButton productId={product.id} productName={product.name} className="absolute right-[14px] bottom-[14px]" />
      </div>
      <div className="mt-[18px] flex items-center justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-[17px] leading-[24px] tracking-tight-2">
            <Link href={`/shop/${product.slug}`}>{product.name}</Link>
          </h3>
          <Price cents={product.priceCents} wasCents={product.compareAtPriceCents} />
        </div>
        {product.imageUrls.length > 1 && (
          <div className="flex shrink-0 gap-[6px]">
            {product.imageUrls.map((url, index) => (
              <button
                key={url}
                type="button"
                aria-label={`Show photo ${index + 1} of ${product.name}`}
                aria-pressed={active === index}
                onClick={() => setActive(index)}
                className={`relative size-9 overflow-hidden rounded-full border ${active === index ? "border-ink" : "border-line"}`}
              >
                <Image src={url} alt="" fill sizes="36px" className="object-cover" />
              </button>
            ))}
          </div>
        )}
      </div>
    </article>
  );
}
