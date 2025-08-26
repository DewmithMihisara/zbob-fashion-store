import Link from "next/link";

export interface FilterTab {
  href: string;
  label: string;
  active: boolean;
}

/** Row of pill tabs inside a soft tray, used by the shop and the blog. */
export function FilterTabs({ tabs, label }: { tabs: FilterTab[]; label: string }) {
  return (
    <nav aria-label={label} className="grid grid-cols-2 gap-[10px] rounded-[36px] bg-line p-[10px] tablet:grid-flow-col tablet:auto-cols-fr tablet:grid-cols-none">
      {tabs.map((tab) => (
        <Link
          key={tab.href}
          href={tab.href}
          scroll={false}
          aria-current={tab.active ? "page" : undefined}
          className={`rounded-full py-[15px] text-center text-[17px] font-medium tracking-tight-2 transition-colors ${
            tab.active ? "bg-ink text-paper" : "bg-paper text-ink hover:bg-canvas"
          }`}
        >
          {tab.label}
        </Link>
      ))}
    </nav>
  );
}
