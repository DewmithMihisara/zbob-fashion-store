"use client";

import { useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { useIsClient, useSession } from "@/hooks/useSession";

/** A convenience for the user only: the API enforces every permission itself. */
export default function DashboardLayout({ children }: { children: ReactNode }) {
  const router = useRouter();
  const session = useSession();
  const isClient = useIsClient();

  useEffect(() => {
    if (isClient && !session) {
      router.replace("/login");
    }
  }, [isClient, session, router]);

  if (!session) {
    return null;
  }
  return (
    <div className="min-h-screen md:flex">
      <Sidebar role={session.role} fullName={session.fullName} />
      <main className="min-w-0 flex-1 p-5 md:p-8">{children}</main>
    </div>
  );
}
