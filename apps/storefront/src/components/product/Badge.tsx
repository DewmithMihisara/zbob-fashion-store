import type { ProductBadge } from "@zbob/shared-types";
import { CrownIcon, SparkleIcon } from "@/components/ui/icons";

export function Badge({ badge }: { badge: ProductBadge }) {
  if (badge === "BEST_SELLER") {
    return (
      <span className="inline-flex items-center gap-[6px] rounded-full bg-ink px-[11px] py-[6px] text-[14px] font-medium text-paper">
        <CrownIcon size={14} />
        Best seller
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-[6px] rounded-full bg-paper px-[11px] py-[6px] text-[14px] text-ink">
      <SparkleIcon size={14} />
      New
    </span>
  );
}
