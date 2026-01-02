"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField, inputClass } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerSession, useIsClient } from "@/hooks/useCustomerSession";
import { customerApi } from "@/lib/customer-api";

export function ReviewForm({ productId }: { productId: number }) {
  const pathname = usePathname();
  const isClient = useIsClient();
  const session = useCustomerSession();
  const [rating, setRating] = useState("5");
  const [headline, setHeadline] = useState("");
  const [body, setBody] = useState("");
  const [sent, setSent] = useState(false);
  const { run, busy, error, fieldErrors } = useCustomerAction();

  if (!isClient) {
    return null;
  }
  if (!session) {
    return (
      <p className="text-[16px] text-muted">
        <Link href={`/login?next=${encodeURIComponent(pathname)}`} className="text-ink underline">
          Sign in
        </Link>{" "}
        to write a review.
      </p>
    );
  }
  if (sent) {
    return (
      <p role="status" className="rounded-card bg-tile p-5 text-[16px]">
        Thanks for your review. It will appear here once we have checked it.
      </p>
    );
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => customerApi("/reviews", { method: "POST", json: { productId, rating: Number(rating), headline, body } }))) {
      setSent(true);
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h3 className="text-[22px] font-medium tracking-tight-3">Write a review</h3>
      <ErrorNotice message={error} />
      <label className="block text-[16px] font-medium">
        Rating
        <select value={rating} onChange={(e) => setRating(e.target.value)} className={inputClass}>
          {[5, 4, 3, 2, 1].map((value) => (
            <option key={value} value={value}>
              {value} out of 5
            </option>
          ))}
        </select>
      </label>
      <FormField label="Headline" required maxLength={200} value={headline} onChange={(e) => setHeadline(e.target.value)} error={fieldErrors.headline} />
      <label className="block text-[16px] font-medium">
        Your review
        <textarea required maxLength={2000} rows={4} value={body} onChange={(e) => setBody(e.target.value)} className={inputClass} />
        {fieldErrors.body && <span className="mt-1 block text-[14px] text-red-700">{fieldErrors.body}</span>}
      </label>
      <button type="submit" disabled={busy} className={buttonClass("dark", "self-start disabled:opacity-60")}>
        {busy ? "Sending..." : "Send review"}
      </button>
    </form>
  );
}
