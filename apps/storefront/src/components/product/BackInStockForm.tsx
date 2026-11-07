"use client";

import { useState, type FormEvent } from "react";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { customerApi } from "@/lib/customer-api";

export function BackInStockForm({ variantId, label }: { variantId: number; label: string }) {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const { run, busy, error } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => customerApi("/stock-alerts", { method: "POST", json: { variantId, email } }))) {
      setSent(true);
    }
  }

  if (sent) {
    return (
      <p role="status" className="rounded-[10px] bg-tile px-[14px] py-[12px] text-[15px]">
        We will email {email} once when {label} is back.
      </p>
    );
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-2 rounded-[10px] bg-tile p-[14px]">
      <p className="text-[15px] font-medium">{label} is sold out. Email me when it is back:</p>
      <ErrorNotice message={error} />
      <div className="flex gap-2">
        <label htmlFor={`alert-${variantId}`} className="sr-only">
          Email for back in stock alert
        </label>
        <input
          id={`alert-${variantId}`}
          type="email"
          required
          maxLength={255}
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          className="h-11 min-w-0 flex-1 rounded-full bg-paper px-4 text-[16px] outline-none focus:outline-2 focus:outline-ink"
        />
        <button type="submit" disabled={busy} className="h-11 rounded-full bg-ink px-5 text-[16px] font-medium text-paper disabled:opacity-60">
          {busy ? "Saving..." : "Notify me"}
        </button>
      </div>
    </form>
  );
}
