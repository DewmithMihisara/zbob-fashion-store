import Image from "next/image";
import { ImagePlaceholder } from "./ImagePlaceholder";

/** Fills its positioned parent with the photo, or a placeholder when there is none yet. */
export function Photo({
  src,
  alt,
  sizes,
  priority,
  tone,
  className = "object-cover",
}: {
  src: string | null | undefined;
  alt: string;
  sizes: string;
  priority?: boolean;
  tone?: "light" | "dark";
  className?: string;
}) {
  if (!src) {
    return <ImagePlaceholder label={alt} tone={tone} className="absolute inset-0" />;
  }
  return <Image src={src} alt={alt} fill sizes={sizes} priority={priority} className={className} />;
}
