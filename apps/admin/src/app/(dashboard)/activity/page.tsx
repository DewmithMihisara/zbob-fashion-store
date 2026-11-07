"use client";

import type { ActivityEntry, Page } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";

const ACTION_LABELS: Record<string, { label: string; tone: "neutral" | "good" | "warn" | "bad" }> = {
  CREATE: { label: "Added", tone: "good" },
  UPDATE: { label: "Changed", tone: "neutral" },
  DELETE: { label: "Deleted", tone: "bad" },
  EXPORT: { label: "Exported", tone: "warn" },
  RESET: { label: "Reset link", tone: "warn" },
  SIGN_IN: { label: "Signed in", tone: "neutral" },
  SIGN_IN_FAILED: { label: "Failed sign in", tone: "bad" },
};

export default function ActivityPage() {
  const session = useSession();
  const [page, setPage] = useState(0);
  const { data, error } = useData<Page<ActivityEntry>>(`/admin/activity?page=${page}`);

  if (session?.role !== "ADMIN") {
    return <p className="text-muted">Only admins can see the activity log.</p>;
  }

  return (
    <>
      <PageHeader title="Activity" description="Every change made in this dashboard, every export, and every dashboard sign in, newest first." />
      <ErrorAlert message={error} />
      <Table headers={["When", "Who", "Action", "What"]}>
        {data?.content.map((entry) => {
          const action = ACTION_LABELS[entry.action] ?? { label: entry.action, tone: "neutral" as const };
          return (
            <tr key={entry.id}>
              <Cell className="whitespace-nowrap text-muted">{new Date(entry.at).toLocaleString()}</Cell>
              <Cell>{entry.actor}</Cell>
              <Cell>
                <StatusBadge tone={action.tone}>{action.label}</StatusBadge>
              </Cell>
              <Cell>{entry.summary}</Cell>
            </tr>
          );
        })}
        {data?.content.length === 0 && <EmptyRow columns={4}>Nothing recorded yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
