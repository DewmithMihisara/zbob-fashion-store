"use client";

import { buttonClass } from "@/components/ui/Button";

export default function ErrorPage({ reset }: { reset: () => void }) {
  return (
    <section className="flex min-h-[70vh] flex-col items-center justify-center gap-4 px-5 pt-24 text-center">
      <h1 className="text-[40px] font-medium tracking-tight-3">Something went wrong</h1>
      <p className="max-w-[420px] text-[16px] text-muted">We could not load this page just now. Please try again in a moment.</p>
      <button type="button" onClick={reset} className={buttonClass("dark")}>
        Try again
      </button>
    </section>
  );
}
