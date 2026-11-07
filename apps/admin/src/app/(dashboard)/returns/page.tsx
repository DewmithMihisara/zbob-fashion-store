"use client";

import type { Page, ReturnRequest, ReturnStatus } from "@zbob/shared-types";
import Link from "next/link";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { TextArea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

const LABELS: Record<ReturnStatus, { label: string; tone: "neutral" | "good" | "warn" | "bad" }> = {
  REQUESTED: { label: "Waiting", tone: "warn" },
  APPROVED: { label: "Approved", tone: "good" },
  REJECTED: { label: "Declined", tone: "bad" },
  COMPLETED: { label: "Completed", tone: "neutral" },
};

/** Mirrors ReturnStatus.canMoveTo in the API, which has the final say. */
const NEXT: Record<ReturnStatus, { status: ReturnStatus; label: string }[]> = {
  REQUESTED: [
    { status: "APPROVED", label: "Approve" },
    { status: "REJECTED", label: "Decline" },
  ],
  APPROVED: [
    { status: "COMPLETED", label: "Mark items received" },
    { status: "REJECTED", label: "Decline" },
  ],
  REJECTED: [],
  COMPLETED: [],
};

function DecideForm({ request, next, onDone }: { request: ReturnRequest; next: { status: ReturnStatus; label: string }; onDone: () => void }) {
  const [note, setNote] = useState("");
  const { run, busy, error } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => api(`/admin/returns/${request.id}`, { method: "PATCH", json: { status: next.status, staffNote: note || null } }))) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <p className="text-[13px] text-muted">
        {request.customerName} asked: “{request.reason}”
      </p>
      <TextArea
        label="Note to the customer"
        rows={3}
        maxLength={1000}
        hint={next.status === "COMPLETED" ? "Optional. Stock is not changed automatically; adjust it on the Stock page if the items can be sold again." : "Optional. Included in the email to the customer."}
        value={note}
        onChange={(e) => setNote(e.target.value)}
      />
      <Button type="submit" disabled={busy} variant={next.status === "REJECTED" ? "danger" : "primary"}>
        {busy ? "Saving..." : next.label}
      </Button>
    </form>
  );
}

export default function ReturnsPage() {
  const [status, setStatus] = useState<ReturnStatus | "">("REQUESTED");
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<ReturnRequest>>(`/admin/returns?page=${page}${status ? `&status=${status}` : ""}`);
  const [deciding, setDeciding] = useState<{ request: ReturnRequest; next: { status: ReturnStatus; label: string } } | null>(null);

  return (
    <>
      <PageHeader title="Returns" description="Requests customers send from their order page. They are emailed when you approve or decline." />
      <select
        aria-label="Filter by status"
        value={status}
        onChange={(e) => {
          setStatus(e.target.value as ReturnStatus | "");
          setPage(0);
        }}
        className="mb-4 h-9 rounded-lg border border-line bg-paper px-3 text-[13px] outline-none focus:border-ink"
      >
        <option value="">All</option>
        {Object.entries(LABELS).map(([value, { label }]) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </select>
      <ErrorAlert message={error} />
      <Table headers={["Order", "Customer", "Reason", "Requested", "Status", ""]}>
        {data?.content.map((request) => (
          <tr key={request.id}>
            <Cell>
              <Link href={`/orders/${request.orderId}`} className="font-medium hover:underline">
                {request.orderNumber}
              </Link>
            </Cell>
            <Cell>
              {request.customerName}
              <span className="block text-[12px] text-muted">{request.customerEmail}</span>
            </Cell>
            <Cell className="max-w-[320px]">
              <span className="line-clamp-2">{request.reason}</span>
              {request.staffNote && <span className="block text-[12px] text-muted">Note: {request.staffNote}</span>}
            </Cell>
            <Cell className="whitespace-nowrap">{new Date(request.requestedAt).toLocaleDateString()}</Cell>
            <Cell>
              <StatusBadge tone={LABELS[request.status].tone}>{LABELS[request.status].label}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                {NEXT[request.status].map((next) => (
                  <Button key={next.status} variant={next.status === "REJECTED" ? "danger" : "secondary"} onClick={() => setDeciding({ request, next })}>
                    {next.label}
                  </Button>
                ))}
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={6}>No return requests here.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {deciding && (
        <Modal title={`${deciding.next.label}: ${deciding.request.orderNumber}`} onClose={() => setDeciding(null)}>
          <DecideForm
            request={deciding.request}
            next={deciding.next}
            onDone={() => {
              setDeciding(null);
              reload();
            }}
          />
        </Modal>
      )}
    </>
  );
}
