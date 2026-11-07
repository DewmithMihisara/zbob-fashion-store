"use client";

import { useCallback, useState } from "react";
import { CustomerApiError, customerErrorMessage } from "@/lib/customer-api";

/** Tracks one action's busy state and errors, including per-field errors from the API. */
export function useCustomerAction() {
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
      setError(customerErrorMessage(e));
      if (e instanceof CustomerApiError) {
        setFieldErrors(e.fieldErrors);
      }
      return false;
    } finally {
      setBusy(false);
    }
  }, []);

  return { run, busy, error, fieldErrors, setError };
}
