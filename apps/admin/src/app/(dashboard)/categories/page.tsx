"use client";

import type { Category, Page } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { TextArea, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

function CategoryForm({ category, onDone }: { category: Category | null; onDone: () => void }) {
  const [name, setName] = useState(category?.name ?? "");
  const [description, setDescription] = useState(category?.description ?? "");
  const [badge, setBadge] = useState(category?.badge ?? "");
  const [position, setPosition] = useState(String(category?.position ?? 0));
  const [sizeGuide, setSizeGuide] = useState(category?.sizeGuide ?? "");
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const json = { name, description: description || null, badge: badge || null, position: Number(position), sizeGuide: sizeGuide || null };
    const ok = await run(() =>
      category
        ? api(`/admin/categories/${category.id}`, { method: "PUT", json })
        : api("/admin/categories", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Name" required maxLength={80} value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
      <TextArea label="Description" rows={3} maxLength={500} hint="Shown on the home page collection card." value={description} onChange={(e) => setDescription(e.target.value)} />
      <TextArea
        label="Size guide"
        rows={6}
        maxLength={10000}
        placeholder={"Size | Chest | Length\nS | 92 cm | 68 cm"}
        hint="Shown on this category's product pages. Put | between columns to make a table; other lines show as notes. Leave empty to hide it."
        value={sizeGuide}
        onChange={(e) => setSizeGuide(e.target.value)}
      />
      <TextField label="Badge" maxLength={20} placeholder="New" value={badge} onChange={(e) => setBadge(e.target.value)} />
      <TextField label="Order" type="number" hint="Lower numbers show first. The first three appear on the home page." value={position} onChange={(e) => setPosition(e.target.value)} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function CategoriesPage() {
  const session = useSession();
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<Category>>(`/admin/categories?page=${page}`);
  const [editing, setEditing] = useState<Category | "new" | null>(null);
  const remove = useAction();

  return (
    <>
      <PageHeader title="Categories" description="The groups used to filter the shop." action={<Button onClick={() => setEditing("new")}>Add category</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Name", "Link", "Badge", "Order", ""]}>
        {data?.content.map((category) => (
          <tr key={category.id}>
            <Cell className="font-medium">{category.name}</Cell>
            <Cell className="text-muted">{category.slug}</Cell>
            <Cell>{category.badge}</Cell>
            <Cell>{category.position}</Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(category)}>
                  Edit
                </Button>
                {session?.role === "ADMIN" && (
                  <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/categories/${category.id}`, { method: "DELETE" })).then(reload)}>
                    Delete
                  </ConfirmButton>
                )}
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No categories yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add category" : "Edit category"} onClose={() => setEditing(null)}>
          <CategoryForm
            category={editing === "new" ? null : editing}
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
