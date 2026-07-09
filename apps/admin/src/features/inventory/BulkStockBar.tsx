"use client";

import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { useAction } from "@/hooks/useAction";
import { api } from "@/lib/api";

/** Sets or adjusts stock for every ticked row in one step. The API applies all of it or none of it. */
export function BulkStockBar({ variantIds, onDone, onClear }: { variantIds: number[]; onDone: () => void; onClear: () => void }) {
  const [mode, setMode] = useState<"SET" | "ADJUST">("ADJUST");
  const [amount, setAmount] = useState("");
  const { run, busy, error } = useAction();
  const parsed = Number(amount);
  const valid = amount.trim() !== "" && Number.isInteger(parsed) && (mode === "ADJUST" ? parsed !== 0 : parsed >= 0);

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => api("/admin/inventory/bulk", { method: "POST", json: { variantIds, mode, amount: parsed } }))) {
      setAmount("");
      onDone();
    }
  }

  return (
    <form onSubmit={submit} aria-label="Change stock for ticked rows" className="mb-4 flex flex-col gap-2 rounded-xl border border-ink bg-paper p-3">
      <div className="flex flex-wrap items-center gap-2 text-[13px]">
        <span className="font-medium">{variantIds.length} selected</span>
        <select
          aria-label="Change type"
          value={mode}
          onChange={(e) => setMode(e.target.value as "SET" | "ADJUST")}
          className="h-9 rounded-lg border border-line px-2 outline-none focus:border-ink"
        >
          <option value="ADJUST">Add or remove</option>
          <option value="SET">Set to</option>
        </select>
        <input
          type="number"
          step={1}
          aria-label="Amount"
          placeholder={mode === "ADJUST" ? "e.g. 10 or -2" : "e.g. 25"}
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          className="h-9 w-32 rounded-lg border border-line px-2 outline-none focus:border-ink"
        />
        <Button type="submit" disabled={!valid || busy}>
          {busy ? "Applying..." : "Apply"}
        </Button>
        <Button variant="secondary" onClick={onClear}>
          Clear selection
        </Button>
      </div>
      <ErrorAlert message={error} />
    </form>
  );
}
