import { site } from "@/lib/site";

const ITEMS = Array.from({ length: 12 }, (_, i) => i);

export function AnnouncementBar() {
  return (
    <div className="h-8 overflow-hidden bg-ink text-paper" aria-label={site.announcement}>
      <div aria-hidden="true" className="marquee flex h-full w-max items-center">
        {ITEMS.map((i) => (
          <span key={i} className="w-[254px] shrink-0 text-center text-[12px] font-medium">
            {site.announcement}
          </span>
        ))}
      </div>
    </div>
  );
}
