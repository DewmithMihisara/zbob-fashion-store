"use client";

import { useEffect, useState } from "react";
import { Photo } from "@/components/ui/Photo";

/** Cycles through a collection's photos, with a dot for each one. */
export function CollectionPhotos({ photos, alt }: { photos: (string | undefined)[]; alt: string }) {
  const [active, setActive] = useState(0);

  useEffect(() => {
    const timer = window.setInterval(() => setActive((current) => (current + 1) % photos.length), 4000);
    return () => window.clearInterval(timer);
  }, [photos.length]);

  return (
    <div className="relative aspect-[567/560] overflow-hidden rounded-card bg-tile">
      {photos.map((photo, index) => (
        <div key={index} className={`absolute inset-0 transition-opacity duration-700 ${index === active ? "opacity-100" : "opacity-0"}`}>
          <Photo src={photo} alt={alt} sizes="(min-width: 810px) 50vw, 100vw" />
        </div>
      ))}
      <div className="absolute bottom-[14px] left-1/2 flex -translate-x-1/2 gap-[5px] rounded-full bg-black/40 px-[10px] py-[8px] backdrop-blur-md">
        {photos.map((_, index) => (
          <button
            key={index}
            type="button"
            aria-label={`Show photo ${index + 1}`}
            aria-pressed={index === active}
            onClick={() => setActive(index)}
            className={`h-[7px] rounded-full bg-paper transition-all ${index === active ? "w-9" : "w-[7px] opacity-70"}`}
          />
        ))}
      </div>
    </div>
  );
}
