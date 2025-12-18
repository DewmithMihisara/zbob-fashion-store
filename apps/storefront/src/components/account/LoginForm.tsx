"use client";

import type { AuthResponse } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerSession } from "@/hooks/useCustomerSession";
import { customerApi } from "@/lib/customer-api";
import { setCustomerSession } from "@/lib/customer-session";
import { safeNext } from "@/lib/safe-next";

export function LoginForm() {
  const router = useRouter();
  const next = safeNext(useSearchParams().get("next"));
  const session = useCustomerSession();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { run, busy, error } = useCustomerAction();

  useEffect(() => {
    if (session) {
      router.replace(next);
    }
  }, [session, next, router]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const auth = await customerApi<AuthResponse>("/auth/customer/login", { method: "POST", json: { email, password } });
      setCustomerSession({ token: auth.accessToken, fullName: auth.fullName });
    });
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Sign in</h1>
      <ErrorNotice message={error} />
      <FormField label="Email" type="email" required autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} />
      <FormField label="Password" type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Signing in..." : "Sign in"}
      </button>
      <p className="text-center text-[15px] text-muted">
        New to zbob?{" "}
        <Link href={`/register?next=${encodeURIComponent(next)}`} className="text-ink underline">
          Create an account
        </Link>
      </p>
    </form>
  );
}
