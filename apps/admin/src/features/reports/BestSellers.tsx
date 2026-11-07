import { formatPrice } from "@zbob/shared-utils";
import type { SalesReport } from "@zbob/shared-types";

/** Horizontal bars, longest first, with the units at the tip and the revenue beside it. */
export function BestSellers({ items }: { items: SalesReport["bestSellers"] }) {
  if (items.length === 0) {
    return <p className="py-8 text-center text-[13px] text-muted">No sales in this period yet.</p>;
  }
  const max = Math.max(...items.map((item) => item.units));
  return (
    <ol className="flex flex-col gap-3" aria-label="Best sellers by units sold">
      {items.map((item) => (
        <li key={item.productName} className="grid grid-cols-[minmax(0,180px)_1fr] items-center gap-3 text-[13px]">
          <span className="truncate" title={item.productName}>
            {item.productName}
          </span>
          <span className="flex items-center gap-2">
            <span className="h-4 rounded-r-[4px] bg-series-1" style={{ width: `${Math.max(2, (item.units / max) * 70)}%` }} aria-hidden="true" />
            <span className="whitespace-nowrap text-ink">
              {item.units} sold <span className="text-muted">· {formatPrice(item.revenueCents)}</span>
            </span>
          </span>
        </li>
      ))}
    </ol>
  );
}
