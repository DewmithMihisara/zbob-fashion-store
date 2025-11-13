"use client";

import type { ShippingMethod } from "@zbob/shared-types";
import { centsToInput, formatPrice, parsePrice } from "@zbob/shared-utils";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

function ShippingForm({ method, onDone }: { method: ShippingMethod | null; onDone: () => void }) {
  const [name, setName] = useState(method?.name ?? "");
  const [description, setDescription] = useState(method?.description ?? "");
  const [price, setPrice] = useState(centsToInput(method?.priceCents ?? 0));
  const [days, setDays] = useState(String(method?.estimatedDays ?? 3));
  const [active, setActive] = useState(method?.active ?? true);
  const [problem, setProblem] = useState<string | null>(null);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const priceCents = parsePrice(price);
    if (priceCents === null) {
      setProblem("Enter a price like 5.00");
      return;
    }
    setProblem(null);
    const json = { name, description: description || null, priceCents, estimatedDays: Number(days), active };
    const ok = await run(() =>
      method
        ? api(`/admin/shipping-methods/${method.id}`, { method: "PUT", json })
        : api("/admin/shipping-methods", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={problem ?? error} />
      <TextField label="Name" required maxLength={80} value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
      <TextField label="Description" maxLength={200} value={description} onChange={(e) => setDescription(e.target.value)} />
      <div className="grid gap-3 sm:grid-cols-2">
        <TextField label="Price" required inputMode="decimal" value={price} onChange={(e) => setPrice(e.target.value)} />
        <TextField label="Estimated days" required type="number" min={0} value={days} onChange={(e) => setDays(e.target.value)} />
      </div>
      <CheckboxField label="Active" checked={active} onChange={(e) => setActive(e.target.checked)} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function ShippingPage() {
  const { data, error, reload } = useData<ShippingMethod[]>("/admin/shipping-methods");
  const [editing, setEditing] = useState<ShippingMethod | "new" | null>(null);
  const remove = useAction();

  return (
    <>
      <PageHeader title="Shipping" description="Delivery options. These apply once online ordering is switched on." action={<Button onClick={() => setEditing("new")}>Add method</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Name", "Price", "Estimated days", "Status", ""]}>
        {data?.map((method) => (
          <tr key={method.id}>
            <Cell className="font-medium">{method.name}</Cell>
            <Cell>{formatPrice(method.priceCents)}</Cell>
            <Cell>{method.estimatedDays}</Cell>
            <Cell>
              <StatusBadge tone={method.active ? "good" : "neutral"}>{method.active ? "Active" : "Off"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(method)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/shipping-methods/${method.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.length === 0 && <EmptyRow columns={5}>No delivery options yet.</EmptyRow>}
      </Table>
      {editing && (
        <Modal title={editing === "new" ? "Add method" : "Edit method"} onClose={() => setEditing(null)}>
          <ShippingForm
            method={editing === "new" ? null : editing}
            onDone={() => {
              setEditing(null);
              reload();
            }}
          />
        </Modal>
      )}
    </>
  );
}
