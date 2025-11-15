"use client";

import type { Page, UserAccount } from "@zbob/shared-types";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export default function CustomersPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<UserAccount>>(`/admin/customers?page=${page}`);
  const action = useAction();

  return (
    <>
      <PageHeader title="Customers" />
      <ErrorAlert message={error ?? action.error} />
      <Table headers={["Name", "Email", "Joined", "Status", ""]}>
        {data?.content.map((customer) => (
          <tr key={customer.id}>
            <Cell className="font-medium">{customer.fullName}</Cell>
            <Cell>{customer.email}</Cell>
            <Cell>{new Date(customer.createdAt).toLocaleDateString()}</Cell>
            <Cell>
              <StatusBadge tone={customer.active ? "good" : "bad"}>{customer.active ? "Active" : "Disabled"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <Button
                variant="secondary"
                disabled={action.busy}
                onClick={() => action.run(() => api(`/admin/customers/${customer.id}?active=${!customer.active}`, { method: "PATCH" })).then(reload)}
              >
                {customer.active ? "Disable" : "Enable"}
              </Button>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No customers yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
