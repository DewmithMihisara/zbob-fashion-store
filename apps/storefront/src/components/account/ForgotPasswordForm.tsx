"use client";

import Link from "next/link";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { customerApi } from "@/lib/customer-api";

export function ForgotPasswordForm() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const { run, busy, error } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => customerApi("/auth/customer/password-reset", { method: "POST", json: { email } }))) {
      setSent(true);
    }
  }

  if (sent) {
    return (
      <div className="flex flex-col gap-4 rounded-card bg-tile p-[22px]" role="status">
        <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Check your email</h1>
        <p className="text-[17px] text-muted">
          If {email} has a zbob account, we have sent it a link to reset the password. The link works for one hour.
        </p>
        <Link href="/login" className="text-[15px] underline">
          Back to sign in
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Forgot your password?</h1>
      <p className="text-[16px] text-muted">Enter your email and we will send you a link to choose a new one.</p>
      <ErrorNotice message={error} />
      <FormField label="Email" type="email" required autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Sending..." : "Send reset link"}
      </button>
      <Link href="/login" className="text-center text-[15px] text-muted underline">
        Back to sign in
      </Link>
    </form>
  );
}
