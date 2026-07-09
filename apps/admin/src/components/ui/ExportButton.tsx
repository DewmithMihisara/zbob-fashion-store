"use client";

import { useAction } from "@/hooks/useAction";
import { downloadFile } from "@/lib/api";
import { Button } from "./Button";

export function ExportButton({ name }: { name: "products" | "inventory" | "customers" | "orders" | "subscribers" }) {
  const { run, busy, error } = useAction();
  return (
    <span className="inline-flex flex-col items-end">
      <Button variant="secondary" disabled={busy} onClick={() => run(() => downloadFile(`/admin/exports/${name}.csv`, `zbob-${name}.csv`))}>
        {busy ? "Preparing..." : "Export CSV"}
      </Button>
      {error && (
        <span role="alert" className="mt-1 text-[12px] text-danger">
          {error}
        </span>
      )}
    </span>
  );
}
