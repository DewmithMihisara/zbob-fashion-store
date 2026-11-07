"use client";

import { useCallback, useState } from "react";
import { ApiRequestError, errorMessage } from "@/lib/api";

/** Tracks one action's busy state and errors, including per-field errors from the API. */
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const run = useCallback(async (action: () => Promise<unknown>): Promise<boolean> => {
    setBusy(true);
    setError(null);
    setFieldErrors({});
    try {
      await action();
      return true;
    } catch (e) {
      setError(errorMessage(e));
      if (e instanceof ApiRequestError) {
        setFieldErrors(e.fieldErrors);
      }
      return false;
    } finally {
      setBusy(false);
    }
  }, []);

  return { run, busy, error, fieldErrors };
}
