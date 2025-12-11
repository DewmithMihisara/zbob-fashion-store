"use client";

import type { Brand, Page } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

function BrandForm({ brand, onDone }: { brand: Brand | null; onDone: () => void }) {
  const [name, setName] = useState(brand?.name ?? "");
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const ok = await run(() =>
      brand
        ? api(`/admin/brands/${brand.id}`, { method: "PUT", json: { name } })
        : api("/admin/brands", { method: "POST", json: { name } }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Name" required maxLength={80} value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function BrandsPage() {
  const session = useSession();
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<Brand>>(`/admin/brands?page=${page}`);
  const [editing, setEditing] = useState<Brand | "new" | null>(null);
  const remove = useAction();

  return (
    <>
      <PageHeader title="Brands" action={<Button onClick={() => setEditing("new")}>Add brand</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Name", "Link", ""]}>
        {data?.content.map((brand) => (
          <tr key={brand.id}>
            <Cell className="font-medium">{brand.name}</Cell>
            <Cell className="text-muted">{brand.slug}</Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(brand)}>
                  Edit
                </Button>
                {session?.role === "ADMIN" && (
                  <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/brands/${brand.id}`, { method: "DELETE" })).then(reload)}>
                    Delete
                  </ConfirmButton>
                )}
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={3}>No brands yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add brand" : "Edit brand"} onClose={() => setEditing(null)}>
          <BrandForm
            brand={editing === "new" ? null : editing}
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
