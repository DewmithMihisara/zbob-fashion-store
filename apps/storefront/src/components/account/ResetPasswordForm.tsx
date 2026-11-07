"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { customerApi } from "@/lib/customer-api";

export function ResetPasswordForm() {
  const token = useSearchParams().get("token") ?? "";
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [done, setDone] = useState(false);
  const { run, busy, error, setError } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (password !== confirm) {
      setError("The two passwords do not match.");
      return;
    }
    if (await run(() => customerApi("/auth/customer/password-reset/confirm", { method: "POST", json: { token, newPassword: password } }))) {
      setDone(true);
    }
  }

  if (!token) {
    return (
      <div className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
        <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Link not complete</h1>
        <p className="text-[17px] text-muted">Open the link from your email again, or ask for a new one.</p>
        <Link href="/forgot-password" className="text-[15px] underline">
          Get a new link
        </Link>
      </div>
    );
  }

  if (done) {
    return (
      <div className="flex flex-col gap-4 rounded-card bg-tile p-[22px]" role="status">
        <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Password changed</h1>
        <p className="text-[17px] text-muted">You can now sign in with your new password.</p>
        <Link href="/login" className={buttonClass("dark", "self-start")}>
          Sign in
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h1 className="text-[34px] leading-[1.15] font-medium tracking-tight-3">Choose a new password</h1>
      <ErrorNotice message={error} />
      <FormField label="New password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" hint="At least 8 characters." value={password} onChange={(e) => setPassword(e.target.value)} />
      <FormField label="Repeat new password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Saving..." : "Save new password"}
      </button>
      {error?.includes("expired") && (
        <Link href="/forgot-password" className="text-center text-[15px] underline">
          Get a new link
        </Link>
      )}
    </form>
  );
}
