"use client";

import type { ProductBadge, ProductImage } from "@zbob/shared-types";
import Image from "next/image";
import { useState } from "react";
import { Photo } from "@/components/ui/Photo";
import { Badge } from "./Badge";

export function ProductGallery({ images, name, badge }: { images: ProductImage[]; name: string; badge: ProductBadge | null }) {
  const [active, setActive] = useState(0);
  const image = images[active];

  return (
    <div className="relative aspect-[575/602] overflow-hidden rounded-card bg-tile">
      <Photo src={image?.url} alt={image?.altText ?? name} sizes="(min-width: 810px) 50vw, 100vw" priority />
      {badge && (
        <span className="absolute top-[18px] left-[18px]">
          <Badge badge={badge} />
        </span>
      )}
      {images.length > 1 && (
        <div className="absolute bottom-[18px] left-1/2 flex -translate-x-1/2 gap-[6px] rounded-[10px] bg-paper p-[6px]">
          {images.map((item, index) => (
            <button
              key={item.id}
              type="button"
              aria-label={`Show photo ${index + 1} of ${name}`}
              aria-pressed={index === active}
              onClick={() => setActive(index)}
              className={`relative h-[60px] overflow-hidden rounded-[6px] transition-all ${
                index === active ? "w-20 ring-1 ring-ink" : "w-11"
              }`}
            >
              <Image src={item.url} alt="" fill sizes="80px" className="object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
