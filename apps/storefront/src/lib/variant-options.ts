import type { PublicVariantOption } from "@zbob/shared-types";

const SIZE_ORDER = ["XXS", "XS", "S", "M", "L", "XL", "XXL", "XXXL"];

/** Sizes in the order people expect: S, M, L rather than alphabetical. Anything else follows, sorted. */
export function sortedSizes(options: PublicVariantOption[]): string[] {
  const rank = (size: string) => {
    const index = SIZE_ORDER.indexOf(size.toUpperCase());
    return index === -1 ? SIZE_ORDER.length : index;
  };
  return [...new Set(options.map((o) => o.size))].sort(
    (a, b) => rank(a) - rank(b) || a.localeCompare(b, undefined, { numeric: true }),
  );
}

export function colors(options: PublicVariantOption[]): string[] {
  return [...new Set(options.map((o) => o.color))].sort((a, b) => a.localeCompare(b));
}

export function find(options: PublicVariantOption[], size: string, color: string): PublicVariantOption | undefined {
  return options.find((o) => o.size === size && o.color === color);
}

/** The first size, in display order, that can be bought. */
export function firstAvailable(options: PublicVariantOption[]): PublicVariantOption | undefined {
  for (const size of sortedSizes(options)) {
    const option = options.find((o) => o.size === size && o.inStock);
    if (option) {
      return option;
    }
  }
  return undefined;
}

/** Choosing a size keeps the current colour if possible, otherwise takes one that is available. */
export function chooseSize(options: PublicVariantOption[], size: string, currentColor: string | undefined): PublicVariantOption | undefined {
  const same = currentColor ? find(options, size, currentColor) : undefined;
  return same?.inStock ? same : options.find((o) => o.size === size && o.inStock);
}
