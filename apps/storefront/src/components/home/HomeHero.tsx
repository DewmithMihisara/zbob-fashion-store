"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { LinkButton } from "@/components/ui/Button";
import { HeroEyebrow } from "@/components/ui/Eyebrow";
import { ImagePlaceholder } from "@/components/ui/ImagePlaceholder";

const SLIDE_LABELS = ["Urban", "Everyday", "Layered", "Winter", "Relaxed", "Classic", "Fresh"];
const INTERVAL_MS = 5000;

export function HomeHero({ photos }: { photos: (string | null)[] }) {
  const [active, setActive] = useState(3);

  useEffect(() => {
    const timer = window.setInterval(() => setActive((current) => (current + 1) % photos.length), INTERVAL_MS);
    return () => window.clearInterval(timer);
  }, [photos.length]);

  return (
    <section className="relative flex min-h-[720px] flex-col overflow-hidden bg-ink text-paper tablet:h-[calc(100vh-32px)]">
      {photos.map((photo, index) => (
        <div key={index} className={`absolute inset-0 transition-opacity duration-700 ${index === active ? "opacity-100" : "opacity-0"}`}>
          {photo ? (
            <Image src={photo} alt="" fill priority={index === active} sizes="100vw" className="object-cover" />
          ) : (
            <ImagePlaceholder tone="dark" />
          )}
        </div>
      ))}
      <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-black/20 to-black/80" />

      <div className="relative flex flex-1 flex-col items-center px-5 pt-[150px] text-center">
        <HeroEyebrow lead="New">The winter layers edit</HeroEyebrow>
        <h1 className="mt-5 text-[42px] leading-[1.13] font-medium tracking-tight-3 tablet:text-[62px]">
          Clothes that keep
          <br />
          up with your day
        </h1>
        <p className="mt-4 max-w-[480px] text-[18px] leading-[27px] font-medium text-paper/75">
          Soft, well-made everyday pieces in fabrics chosen to feel good from morning until late.
        </p>
        <div className="mt-6 flex flex-wrap justify-center gap-[10px]">
          <LinkButton href="/shop" variant="light">
            See all collections
          </LinkButton>
          <LinkButton href="/contact" variant="glass">
            Contact us
          </LinkButton>
        </div>
      </div>

      <div className="relative flex items-end justify-center gap-[5px] px-5 pb-6">
        {photos.map((photo, index) => (
          <button
            key={index}
            type="button"
            aria-label={`Show ${SLIDE_LABELS[index]} photo`}
            aria-pressed={index === active}
            onClick={() => setActive(index)}
            className={`relative overflow-hidden transition-all duration-300 ${
              index === active ? "h-[60px] w-[80px] border border-paper tablet:h-[105px] tablet:w-[140px]" : "h-10 w-10 opacity-90 tablet:h-[70px] tablet:w-[88px]"
            }`}
          >
            {photo ? <Image src={photo} alt="" fill sizes="140px" className="object-cover" /> : <ImagePlaceholder tone="dark" className="[&>span]:hidden" />}
            {index === active && (
              <span className="absolute inset-x-0 bottom-2 text-center text-[13px] font-medium">{SLIDE_LABELS[index]}</span>
            )}
          </button>
        ))}
      </div>
    </section>
  );
}
