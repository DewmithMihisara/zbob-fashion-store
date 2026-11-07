"use client";

import { useState, type FormEvent } from "react";
import { postJson } from "@/lib/client-api";

type Status = "idle" | "sending" | "done" | "error";

export function NewsletterForm() {
  const [email, setEmail] = useState("");
  const [status, setStatus] = useState<Status>("idle");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setStatus("sending");
    const ok = await postJson("/newsletter", { email });
    setStatus(ok ? "done" : "error");
    if (ok) {
      setEmail("");
    }
  }

  return (
    <form onSubmit={submit} className="w-full tablet:w-auto">
      <div className="flex gap-[10px]">
        <label htmlFor="newsletter-email" className="sr-only">
          Email address
        </label>
        <input
          id="newsletter-email"
          type="email"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="Enter your email"
          className="h-12 min-w-0 flex-1 rounded-full bg-paper/10 px-[25px] text-[17px] text-paper placeholder:text-paper/50 tablet:w-[267px] tablet:flex-none"
        />
        <button
          type="submit"
          disabled={status === "sending"}
          className="h-12 rounded-full bg-paper px-[25px] text-[17px] text-ink transition-transform hover:scale-[1.03] disabled:opacity-60"
        >
          Subscribe
        </button>
      </div>
      <p aria-live="polite" className="mt-2 min-h-5 text-[14px] text-paper/60">
        {status === "done" && "Thanks, you are on the list."}
        {status === "error" && "That did not work. Please try again."}
      </p>
    </form>
  );
}
