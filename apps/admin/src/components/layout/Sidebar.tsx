"use client";

import type { Role } from "@zbob/shared-types";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { clearSession } from "@/lib/session";
import { navigationFor } from "./navigation";

export function Sidebar({ role, fullName }: { role: Role; fullName: string }) {
  const pathname = usePathname();
  return (
    <aside className="flex w-full shrink-0 flex-col bg-ink text-paper md:sticky md:top-0 md:h-screen md:w-[220px]">
      <p className="px-5 py-5 text-[18px] font-semibold tracking-wide uppercase">zbob</p>
      <nav aria-label="Admin" className="flex-1 overflow-y-auto px-3 pb-4">
        {navigationFor(role).map((group) => (
          <div key={group.title} className="mb-4">
            <p className="px-2 pb-1 text-[11px] tracking-wide text-paper/40 uppercase">{group.title}</p>
            {group.items.map((item) => {
              const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  aria-current={active ? "page" : undefined}
                  className={`block rounded-lg px-2 py-[7px] text-[13px] transition-colors ${
                    active ? "bg-paper/15 font-medium" : "text-paper/70 hover:bg-paper/10 hover:text-paper"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>
      <div className="border-t border-paper/15 px-5 py-4 text-[13px]">
        <p className="truncate">{fullName}</p>
        <p className="text-[12px] text-paper/50">{role === "ADMIN" ? "Admin" : "Staff"}</p>
        <button type="button" onClick={clearSession} className="mt-2 text-[12px] text-paper/70 underline hover:text-paper">
          Sign out
        </button>
      </div>
    </aside>
  );
}
