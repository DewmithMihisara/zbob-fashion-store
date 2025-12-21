"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { Container, Heading } from "@/components/ui/SectionHeading";
import { useCustomerSession, useIsClient } from "@/hooks/useCustomerSession";
import { clearCustomerSession } from "@/lib/customer-session";

const TABS = [
  { href: "/account", label: "Profile" },
  { href: "/account/addresses", label: "Addresses" },
  { href: "/account/orders", label: "Orders" },
];

/** Sends signed out visitors to sign in. A convenience only: the API protects the data itself. */
export function AccountShell({ children }: { children: ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const session = useCustomerSession();
  const isClient = useIsClient();

  useEffect(() => {
    if (isClient && !session) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [isClient, session, pathname, router]);

  if (!session) {
    return null;
  }

  return (
    <section className="pt-[130px] pb-20">
      <Container>
        <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
          <Heading>Hello, {session.fullName}</Heading>
          <button type="button" onClick={clearCustomerSession} className="text-[15px] text-muted underline hover:text-ink">
            Sign out
          </button>
        </div>
        <nav aria-label="Account" className="mb-8 flex flex-wrap gap-2">
          {TABS.map((tab) => {
            const active = tab.href === "/account" ? pathname === "/account" : pathname.startsWith(tab.href);
            return (
              <Link
                key={tab.href}
                href={tab.href}
                aria-current={active ? "page" : undefined}
                className={`rounded-full px-[18px] py-[10px] text-[16px] font-medium tracking-tight-2 transition-colors ${active ? "bg-ink text-paper" : "bg-paper text-ink hover:bg-tile"}`}
              >
                {tab.label}
              </Link>
            );
          })}
        </nav>
        {children}
      </Container>
    </section>
  );
}
