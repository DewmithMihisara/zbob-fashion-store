"use client";

import type { Page, UserAccount } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { ExportButton } from "@/components/ui/ExportButton";
import { CheckboxField, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

function CustomerForm({ customer, onDone }: { customer: UserAccount | null; onDone: () => void }) {
  const [fullName, setFullName] = useState(customer?.fullName ?? "");
  const [email, setEmail] = useState(customer?.email ?? "");
  const [password, setPassword] = useState("");
  const [active, setActive] = useState(customer?.active ?? true);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const ok = await run(() =>
      customer
        ? api(`/admin/customers/${customer.id}`, { method: "PUT", json: { fullName, email, active } })
        : api("/admin/customers", { method: "POST", json: { fullName, email, password } }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Full name" required maxLength={120} value={fullName} onChange={(e) => setFullName(e.target.value)} error={fieldErrors.fullName} />
      <TextField label="Email" type="email" required maxLength={255} value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
      {customer ? (
        <CheckboxField label="Can sign in" checked={active} onChange={(e) => setActive(e.target.checked)} />
      ) : (
        <TextField label="Password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" hint="At least 8 characters." value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} />
      )}
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function CustomersPage() {
  const session = useSession();
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<UserAccount>>(`/admin/customers?page=${page}`);
  const [editing, setEditing] = useState<UserAccount | "new" | null>(null);
  const action = useAction();

  return (
    <>
      <PageHeader
        title="Customers"
        description="A customer with orders can be disabled but not deleted."
        action={
          <span className="inline-flex items-start gap-2">
            {session?.role === "ADMIN" && <ExportButton name="customers" />}
            <Button onClick={() => setEditing("new")}>Add customer</Button>
          </span>
        }
      />
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
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(customer)}>
                  Edit
                </Button>
                {session?.role === "ADMIN" && (
                  <ConfirmButton confirmLabel="Yes" onConfirm={() => action.run(() => api(`/admin/customers/${customer.id}`, { method: "DELETE" })).then(reload)}>
                    Delete
                  </ConfirmButton>
                )}
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No customers yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add customer" : `Edit ${editing.fullName}`} onClose={() => setEditing(null)}>
          <CustomerForm
            customer={editing === "new" ? null : editing}
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
