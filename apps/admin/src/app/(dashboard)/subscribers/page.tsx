"use client";

import type { Page, Subscriber } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useData } from "@/hooks/useData";

export default function SubscribersPage() {
  const [page, setPage] = useState(0);
  const { data, error } = useData<Page<Subscriber>>(`/admin/newsletter-subscribers?page=${page}`);

  return (
    <>
      <PageHeader title="Subscribers" description="People who signed up for news from the website footer." />
      <ErrorAlert message={error} />
      <Table headers={["Email", "Subscribed"]}>
        {data?.content.map((subscriber) => (
          <tr key={subscriber.id}>
            <Cell className="font-medium">{subscriber.email}</Cell>
            <Cell>{new Date(subscriber.subscribedAt).toLocaleDateString()}</Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={2}>No subscribers yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
