"use client";

import type { ContactMessage, Page } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField, TextArea, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

function MessageForm({ message, onDone }: { message: ContactMessage | null; onDone: () => void }) {
  const [name, setName] = useState(message?.name ?? "");
  const [email, setEmail] = useState(message?.email ?? "");
  const [subject, setSubject] = useState(message?.subject ?? "");
  const [text, setText] = useState(message?.message ?? "");
  const [handled, setHandled] = useState(message?.handled ?? false);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const json = { name, email, subject: subject || null, message: text, handled };
    const ok = await run(() =>
      message
        ? api(`/admin/contact-messages/${message.id}`, { method: "PUT", json })
        : api("/admin/contact-messages", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Name" required maxLength={120} value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
      <TextField label="Email" type="email" required maxLength={255} value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
      <TextField label="Subject" maxLength={200} value={subject} onChange={(e) => setSubject(e.target.value)} />
      <TextArea label="Message" required rows={5} maxLength={4000} value={text} onChange={(e) => setText(e.target.value)} error={fieldErrors.message} />
      <CheckboxField label="Done" checked={handled} onChange={(e) => setHandled(e.target.checked)} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function MessagesPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<ContactMessage>>(`/admin/contact-messages?page=${page}`);
  const [editing, setEditing] = useState<ContactMessage | "new" | null>(null);
  const action = useAction();

  return (
    <>
      <PageHeader title="Messages" description="Sent from the contact form on the website, or added here for enquiries that came in another way." action={<Button onClick={() => setEditing("new")}>Add message</Button>} />
      <ErrorAlert message={error ?? action.error} />
      <ul className="flex flex-col gap-3">
        {data?.content.map((message) => (
          <li key={message.id} className="rounded-xl border border-line bg-paper p-4">
            <div className="flex flex-wrap items-start justify-between gap-2">
              <div>
                <p className="font-medium">{message.subject || "(No subject)"}</p>
                <p className="text-[13px] text-muted">
                  {message.name} · <a href={`mailto:${message.email}`} className="underline">{message.email}</a> · {new Date(message.receivedAt).toLocaleString()}
                </p>
              </div>
              <StatusBadge tone={message.handled ? "neutral" : "warn"}>{message.handled ? "Done" : "New"}</StatusBadge>
            </div>
            <p className="mt-3 text-[14px] whitespace-pre-wrap">{message.message}</p>
            <div className="mt-3 flex gap-1">
              <Button
                variant="secondary"
                disabled={action.busy}
                onClick={() => action.run(() => api(`/admin/contact-messages/${message.id}?handled=${!message.handled}`, { method: "PATCH" })).then(reload)}
              >
                {message.handled ? "Mark as new" : "Mark as done"}
              </Button>
              <Button variant="secondary" onClick={() => setEditing(message)}>
                Edit
              </Button>
              <ConfirmButton confirmLabel="Yes" onConfirm={() => action.run(() => api(`/admin/contact-messages/${message.id}`, { method: "DELETE" })).then(reload)}>
                Delete
              </ConfirmButton>
            </div>
          </li>
        ))}
      </ul>
      {data?.content.length === 0 && <p className="py-10 text-center text-muted">No messages yet.</p>}
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add message" : "Edit message"} onClose={() => setEditing(null)}>
          <MessageForm
            message={editing === "new" ? null : editing}
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
