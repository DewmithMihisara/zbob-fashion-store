"use client";

import { useState, type FormEvent } from "react";
import { postJson } from "@/lib/client-api";

const inputClass =
  "mt-2 w-full rounded-[10px] bg-canvas px-[14px] py-[13px] text-[17px] placeholder:text-faint focus:outline-2 focus:outline-ink";

type Status = "idle" | "sending" | "done" | "error";

export function ContactForm() {
  const [status, setStatus] = useState<Status>("idle");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = Object.fromEntries(new FormData(form));
    setStatus("sending");
    const ok = await postJson("/contact", data);
    setStatus(ok ? "done" : "error");
    if (ok) {
      form.reset();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      {/* Hidden from people; bots that fill it in are ignored by the API. */}
      <input type="text" name="website" tabIndex={-1} autoComplete="off" aria-hidden="true" className="hidden" />
      <label className="text-[17px] font-medium">
        Full name
        <input name="name" required maxLength={120} placeholder="Your name" className={inputClass} />
      </label>
      <label className="text-[17px] font-medium">
        Email
        <input name="email" type="email" required maxLength={255} placeholder="you@example.com" className={inputClass} />
      </label>
      <label className="text-[17px] font-medium">
        Subject
        <input name="subject" maxLength={200} placeholder="Enquiry" className={inputClass} />
      </label>
      <label className="text-[17px] font-medium">
        Message
        <textarea name="message" required maxLength={4000} rows={6} placeholder="Enter message here..." className={inputClass} />
      </label>
      <button
        type="submit"
        disabled={status === "sending"}
        className="h-12 rounded-full bg-ink text-[17px] font-medium tracking-tight-2 text-paper transition-transform hover:scale-[1.01] disabled:opacity-60"
      >
        {status === "sending" ? "Sending..." : "Send Message"}
      </button>
      <p aria-live="polite" className="min-h-5 text-center text-[15px] text-muted">
        {status === "done" && "Thanks, your message has been sent."}
        {status === "error" && "Your message could not be sent. Please try again."}
      </p>
    </form>
  );
}
