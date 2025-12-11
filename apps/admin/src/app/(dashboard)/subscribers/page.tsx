"use client";

import type { Page, Subscriber } from "@zbob/shared-types";
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
import { api } from "@/lib/api";

function SubscriberForm({ subscriber, onDone }: { subscriber: Subscriber | null; onDone: () => void }) {
  const [email, setEmail] = useState(subscriber?.email ?? "");
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const ok = await run(() =>
      subscriber
        ? api(`/admin/newsletter-subscribers/${subscriber.id}`, { method: "PUT", json: { email } })
        : api("/admin/newsletter-subscribers", { method: "POST", json: { email } }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Email" type="email" required maxLength={255} value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function SubscribersPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<Subscriber>>(`/admin/newsletter-subscribers?page=${page}`);
  const [editing, setEditing] = useState<Subscriber | "new" | null>(null);
  const remove = useAction();

  return (
    <>
      <PageHeader title="Subscribers" description="People who signed up for news from the website footer." action={<Button onClick={() => setEditing("new")}>Add subscriber</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Email", "Subscribed", ""]}>
        {data?.content.map((subscriber) => (
          <tr key={subscriber.id}>
            <Cell className="font-medium">{subscriber.email}</Cell>
            <Cell>{new Date(subscriber.subscribedAt).toLocaleDateString()}</Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(subscriber)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/newsletter-subscribers/${subscriber.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={3}>No subscribers yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add subscriber" : "Edit subscriber"} onClose={() => setEditing(null)}>
          <SubscriberForm
            subscriber={editing === "new" ? null : editing}
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
