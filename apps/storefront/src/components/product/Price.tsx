import { formatPrice } from "@zbob/shared-utils";

export function Price({
  cents,
  wasCents,
  prefix = "",
  className = "text-[22px]",
  wasClassName = "text-[15px]",
}: {
  cents: number;
  wasCents: number | null;
  prefix?: string;
  className?: string;
  wasClassName?: string;
}) {
  return (
    <span className="flex items-baseline gap-[10px]">
      <span className={`font-medium tracking-tight-2 ${className}`}>
        {prefix}
        {formatPrice(cents)}
      </span>
      {wasCents !== null && wasCents > cents && (
        <s className={`font-medium text-faint ${wasClassName}`}>
          <span className="sr-only">Was </span>
          {prefix}
          {formatPrice(wasCents)}
        </s>
      )}
    </span>
  );
}
