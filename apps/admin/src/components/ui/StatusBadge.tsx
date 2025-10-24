import type { ReactNode } from "react";

type Tone = "neutral" | "good" | "warn" | "bad";

const tones: Record<Tone, string> = {
  neutral: "bg-tile text-muted",
  good: "bg-green-50 text-success",
  warn: "bg-orange-50 text-accent",
  bad: "bg-red-50 text-danger",
};

export function StatusBadge({ tone = "neutral", children }: { tone?: Tone; children: ReactNode }) {
  return <span className={`inline-block rounded-full px-2 py-[3px] text-[12px] font-medium whitespace-nowrap ${tones[tone]}`}>{children}</span>;
}
