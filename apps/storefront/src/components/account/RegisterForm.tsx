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

export function RegisterForm() {
  const router = useRouter();
  const next = safeNext(useSearchParams().get("next"));
  const session = useCustomerSession();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const { run, busy, error, fieldErrors } = useCustomerAction();

  useEffect(() => {
    if (session) {
      router.replace(next);
    }
  }, [session, next, router]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    await run(async () => {
      const auth = await customerApi<AuthResponse>("/auth/customer/register", { method: "POST", json: { fullName, email, password } });
      setCustomerSession({ token: auth.accessToken, fullName: auth.fullName });
    });
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Create your account</h1>
      <ErrorNotice message={error} />
      <FormField label="Full name" required maxLength={120} autoComplete="name" value={fullName} onChange={(e) => setFullName(e.target.value)} error={fieldErrors.fullName} />
      <FormField label="Email" type="email" required maxLength={255} autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
      <FormField label="Password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" hint="At least 8 characters." value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Creating..." : "Create account"}
      </button>
      <p className="text-center text-[15px] text-muted">
        Already have an account?{" "}
        <Link href={`/login?next=${encodeURIComponent(next)}`} className="text-ink underline">
          Sign in
        </Link>
      </p>
    </form>
  );
}
