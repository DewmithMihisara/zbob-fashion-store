"use client";

import type { DiscountType, Page, Promotion } from "@zbob/shared-types";
import { centsToInput, formatPrice, parsePrice } from "@zbob/shared-utils";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField, SelectField, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

/** datetime-local inputs work in local time without a zone; the API stores UTC instants. */
function toInputDate(iso: string | null): string {
  if (!iso) {
    return "";
  }
  const date = new Date(iso);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
}

function fromInputDate(value: string): string | null {
  return value ? new Date(value).toISOString() : null;
}

function describe(promotion: Promotion): string {
  return promotion.discountType === "PERCENT" ? `${promotion.discountValue}% off` : `${formatPrice(promotion.discountValue)} off`;
}

function PromotionForm({ promotion, onDone }: { promotion: Promotion | null; onDone: () => void }) {
  const [code, setCode] = useState(promotion?.code ?? "");
  const [description, setDescription] = useState(promotion?.description ?? "");
  const [discountType, setDiscountType] = useState<DiscountType>(promotion?.discountType ?? "PERCENT");
  const [value, setValue] = useState(
    promotion ? (promotion.discountType === "PERCENT" ? String(promotion.discountValue) : centsToInput(promotion.discountValue)) : "",
  );
  const [minimum, setMinimum] = useState(centsToInput(promotion?.minimumCents ?? 0));
  const [startsAt, setStartsAt] = useState(toInputDate(promotion?.startsAt ?? null));
  const [endsAt, setEndsAt] = useState(toInputDate(promotion?.endsAt ?? null));
  const [active, setActive] = useState(promotion?.active ?? true);
  const [problem, setProblem] = useState<string | null>(null);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const discountValue = discountType === "PERCENT" ? Number(value) : parsePrice(value);
    const minimumCents = parsePrice(minimum);
    if (!discountValue || minimumCents === null) {
      setProblem("Enter a valid discount and minimum spend.");
      return;
    }
    setProblem(null);
    const json = {
      code,
      description: description || null,
      discountType,
      discountValue,
      minimumCents,
      startsAt: fromInputDate(startsAt),
      endsAt: fromInputDate(endsAt),
      active,
    };
    const ok = await run(() =>
      promotion
        ? api(`/admin/promotions/${promotion.id}`, { method: "PUT", json })
        : api("/admin/promotions", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={problem ?? error} />
      <TextField label="Code" required maxLength={40} value={code} onChange={(e) => setCode(e.target.value)} error={fieldErrors.code} />
      <TextField label="Description" maxLength={200} value={description} onChange={(e) => setDescription(e.target.value)} />
      <div className="grid gap-3 sm:grid-cols-2">
        <SelectField label="Discount type" value={discountType} onChange={(e) => setDiscountType(e.target.value as DiscountType)}>
          <option value="PERCENT">Percent</option>
          <option value="FIXED">Fixed amount</option>
        </SelectField>
        <TextField label={discountType === "PERCENT" ? "Percent off" : "Amount off"} required inputMode="decimal" value={value} onChange={(e) => setValue(e.target.value)} />
        <TextField label="Starts" type="datetime-local" value={startsAt} onChange={(e) => setStartsAt(e.target.value)} />
        <TextField label="Ends" type="datetime-local" value={endsAt} onChange={(e) => setEndsAt(e.target.value)} />
      </div>
      <TextField label="Minimum spend" required inputMode="decimal" value={minimum} onChange={(e) => setMinimum(e.target.value)} />
      <CheckboxField label="Active" checked={active} onChange={(e) => setActive(e.target.checked)} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function PromotionsPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<Promotion>>(`/admin/promotions?page=${page}`);
  const [editing, setEditing] = useState<Promotion | "new" | null>(null);
  const remove = useAction();

  return (
    <>
      <PageHeader title="Promotions" description="Discount codes. These apply once online ordering is switched on." action={<Button onClick={() => setEditing("new")}>Add promotion</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Code", "Discount", "Minimum spend", "Status", ""]}>
        {data?.content.map((promotion) => (
          <tr key={promotion.id}>
            <Cell className="font-medium">{promotion.code}</Cell>
            <Cell>{describe(promotion)}</Cell>
            <Cell>{formatPrice(promotion.minimumCents)}</Cell>
            <Cell>
              <StatusBadge tone={promotion.active ? "good" : "neutral"}>{promotion.active ? "Active" : "Off"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(promotion)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/promotions/${promotion.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No promotions yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add promotion" : "Edit promotion"} onClose={() => setEditing(null)}>
          <PromotionForm
            promotion={editing === "new" ? null : editing}
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
