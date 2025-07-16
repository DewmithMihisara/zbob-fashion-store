import type { ReactNode } from "react";

/** Two-part pill: a highlighted lead ("Shop") and a label ("The new season"). */
export function HeroEyebrow({ lead, children }: { lead: string; children: ReactNode }) {
  return (
    <span className="inline-flex items-center gap-2 rounded-full bg-paper/15 p-[3px] pr-[13px] text-[13px] font-medium text-paper backdrop-blur-md">
      <span className="rounded-full bg-paper px-[10px] py-[4px] text-[12px] text-ink">{lead}</span>
      {children}
    </span>
  );
}

/** Light section label: a round icon chip and text, or a dark tag and text. */
export function SectionEyebrow({ icon, children }: { icon: ReactNode; children: ReactNode }) {
  return (
    <span className="inline-flex items-center gap-[6px] rounded-full bg-paper p-[3px] pr-[13px] text-[14px] text-ink">
      <span className="flex size-6 items-center justify-center rounded-full bg-ink text-paper">{icon}</span>
      {children}
    </span>
  );
}
