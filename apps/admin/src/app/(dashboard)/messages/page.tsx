"use client";

import type { ContactMessage, Page } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export default function MessagesPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<ContactMessage>>(`/admin/contact-messages?page=${page}`);
  const action = useAction();

  return (
    <>
      <PageHeader title="Messages" description="Sent from the contact form on the website." />
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
              <ConfirmButton confirmLabel="Yes" onConfirm={() => action.run(() => api(`/admin/contact-messages/${message.id}`, { method: "DELETE" })).then(reload)}>
                Delete
              </ConfirmButton>
            </div>
          </li>
        ))}
      </ul>
      {data?.content.length === 0 && <p className="py-10 text-center text-muted">No messages yet.</p>}
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
