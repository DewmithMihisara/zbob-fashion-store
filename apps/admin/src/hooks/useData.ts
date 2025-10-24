"use client";

import { useCallback, useEffect, useState } from "react";
import { api, errorMessage } from "@/lib/api";

interface Loaded<T> {
  path: string;
  version: number;
  data?: T;
  error?: string;
}

/** Loads a path and reloads when it changes. Late responses from older requests are ignored. */
export function useData<T>(path: string) {
  const [version, setVersion] = useState(0);
  const [loaded, setLoaded] = useState<Loaded<T> | null>(null);

  useEffect(() => {
    let current = true;
    api<T>(path).then(
      (data) => {
        if (current) setLoaded({ path, version, data });
      },
      (error: unknown) => {
        if (current) setLoaded({ path, version, error: errorMessage(error) });
      },
    );
    return () => {
      current = false;
    };
  }, [path, version]);

  const reload = useCallback(() => setVersion((v) => v + 1), []);
  const samePath = loaded?.path === path;

  return {
    // Keeps showing the old data during a reload of the same path, so tables do not flash.
    data: samePath ? loaded?.data : undefined,
    error: samePath ? loaded?.error : undefined,
    loading: !samePath || loaded?.version !== version,
    reload,
  };
}
