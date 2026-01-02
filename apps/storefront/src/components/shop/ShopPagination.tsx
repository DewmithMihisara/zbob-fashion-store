import Link from "next/link";
import { shopHref, type ShopQuery } from "@/lib/shop-query";

/** Page numbers near the current page, with the first and last always reachable. */
export function pageNumbers(current: number, total: number): (number | "gap")[] {
  const wanted = new Set([1, total, current - 1, current, current + 1].filter((n) => n >= 1 && n <= total));
  const sorted = [...wanted].sort((a, b) => a - b);
  const result: (number | "gap")[] = [];
  sorted.forEach((n, i) => {
    if (i > 0 && n - sorted[i - 1]! > 1) {
      result.push("gap");
    }
    result.push(n);
  });
  return result;
}

export function ShopPagination({ query, totalPages }: { query: ShopQuery; totalPages: number }) {
  if (totalPages <= 1) {
    return null;
  }
  const pill = "flex h-11 min-w-11 items-center justify-center rounded-full px-3 text-[16px] font-medium";
  return (
    <nav aria-label="Pages" className="mt-12 flex flex-wrap items-center justify-center gap-2">
      {query.page > 1 && (
        <Link href={shopHref(query, { page: query.page - 1 })} className={`${pill} bg-paper hover:bg-tile`}>
          Previous
        </Link>
      )}
      {pageNumbers(query.page, totalPages).map((n, i) =>
        n === "gap" ? (
          <span key={`gap-${i}`} className="px-1 text-muted">
            …
          </span>
        ) : (
          <Link
            key={n}
            href={shopHref(query, { page: n })}
            aria-current={n === query.page ? "page" : undefined}
            aria-label={`Page ${n}`}
            className={`${pill} ${n === query.page ? "bg-ink text-paper" : "bg-paper hover:bg-tile"}`}
          >
            {n}
          </Link>
        ),
      )}
      {query.page < totalPages && (
        <Link href={shopHref(query, { page: query.page + 1 })} className={`${pill} bg-paper hover:bg-tile`}>
          Next
        </Link>
      )}
    </nav>
  );
}
