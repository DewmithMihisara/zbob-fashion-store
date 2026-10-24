import type { SalesReport } from "@zbob/shared-types";
import Link from "next/link";

const PARTS = [
  { key: "inStock", label: "In stock", icon: "✓", color: "var(--color-status-good)" },
  { key: "low", label: "Low stock", icon: "!", color: "var(--color-status-warning)" },
  { key: "out", label: "Out of stock", icon: "×", color: "var(--color-status-critical)" },
] as const;

/** One whole split into parts. Status colours always come with an icon, a label and the count. */
export function StockHealth({ stock }: { stock: SalesReport["stock"] }) {
  const total = stock.inStock + stock.low + stock.out;
  return (
    <div>
      {total > 0 && (
        <div className="flex h-3 gap-[2px] overflow-hidden rounded-full" role="img" aria-label={`${stock.inStock} in stock, ${stock.low} low, ${stock.out} out of stock`}>
          {PARTS.filter((part) => stock[part.key] > 0).map((part) => (
            <span key={part.key} style={{ width: `${(stock[part.key] / total) * 100}%`, backgroundColor: part.color }} />
          ))}
        </div>
      )}
      <ul className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-[13px]">
        {PARTS.map((part) => (
          <li key={part.key} className="flex items-center gap-2">
            <span className="flex size-4 items-center justify-center rounded-full text-[10px] font-bold text-paper" style={{ backgroundColor: part.color }} aria-hidden="true">
              {part.icon}
            </span>
            {part.label} <span className="font-semibold">{stock[part.key]}</span>
          </li>
        ))}
      </ul>
      <Link href="/inventory?low=1" className="mt-3 inline-block text-[13px] underline">
        See low and out of stock
      </Link>
    </div>
  );
}
