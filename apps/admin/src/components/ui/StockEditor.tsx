"use client";

import { useState } from "react";
import { api, errorMessage } from "@/lib/api";
import { Button } from "./Button";

/** Sets the exact count for one size and colour. Save appears only when the number has changed. */
export function StockEditor({
  variantId,
  quantity,
  label,
  onSaved,
  onError,
}: {
  variantId: number;
  quantity: number;
  label: string;
  onSaved: () => void;
  onError: (message: string) => void;
}) {
  const [value, setValue] = useState(String(quantity));
  const [busy, setBusy] = useState(false);
  const parsed = Number(value);
  const valid = value.trim() !== "" && Number.isInteger(parsed) && parsed >= 0;

  async function save() {
    setBusy(true);
    try {
      await api(`/admin/variants/${variantId}/stock`, { method: "PATCH", json: { stockQuantity: parsed } });
      onSaved();
    } catch (e) {
      onError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <span className="inline-flex items-center gap-1">
      <input
        type="number"
        min={0}
        step={1}
        aria-label={`Stock for ${label}`}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        className="h-9 w-20 rounded-lg border border-line px-2 text-[13px] outline-none focus:border-ink aria-invalid:border-danger"
        aria-invalid={!valid}
      />
      {parsed !== quantity && (
        <Button disabled={!valid || busy} onClick={save}>
          Save
        </Button>
      )}
    </span>
  );
}
