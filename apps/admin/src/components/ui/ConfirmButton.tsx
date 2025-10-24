"use client";

import { useState, type ReactNode } from "react";
import { Button } from "./Button";

/** Asks once more on the button itself, so a stray click can not delete anything. */
export function ConfirmButton({
  onConfirm,
  children,
  confirmLabel = "Confirm delete",
  disabled,
}: {
  onConfirm: () => void;
  children: ReactNode;
  confirmLabel?: string;
  disabled?: boolean;
}) {
  const [asking, setAsking] = useState(false);

  if (!asking) {
    return (
      <Button variant="danger" disabled={disabled} onClick={() => setAsking(true)}>
        {children}
      </Button>
    );
  }
  return (
    <span className="inline-flex gap-1">
      <Button
        variant="danger"
        onClick={() => {
          setAsking(false);
          onConfirm();
        }}
      >
        {confirmLabel}
      </Button>
      <Button variant="secondary" onClick={() => setAsking(false)}>
        Cancel
      </Button>
    </span>
  );
}
