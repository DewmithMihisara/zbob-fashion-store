"use client";

import type { Page, Role, UserAccount } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert, SavedAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField, SelectField, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { useSession } from "@/hooks/useSession";
import { api } from "@/lib/api";

function NewStaffForm({ onDone }: { onDone: () => void }) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<Role>("STAFF");
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await run(() => api("/admin/staff", { method: "POST", json: { fullName, email, password, role } }))) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Full name" required maxLength={120} value={fullName} onChange={(e) => setFullName(e.target.value)} error={fieldErrors.fullName} />
      <TextField label="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} error={fieldErrors.email} />
      <TextField label="Password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" hint="At least 8 characters." value={password} onChange={(e) => setPassword(e.target.value)} error={fieldErrors.password} />
      <SelectField label="Role" value={role} onChange={(e) => setRole(e.target.value as Role)}>
        <option value="STAFF">Staff: manages products, stock and content</option>
        <option value="ADMIN">Admin: also deletes products and manages staff</option>
      </SelectField>
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Add staff member"}
      </Button>
    </form>
  );
}

function EditStaffForm({ member, onDone }: { member: UserAccount; onDone: () => void }) {
  const [fullName, setFullName] = useState(member.fullName);
  const [role, setRole] = useState<Role>(member.role);
  const [active, setActive] = useState(member.active);
  const [password, setPassword] = useState("");
  const [passwordSaved, setPasswordSaved] = useState(false);
  const details = useAction();
  const reset = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (await details.run(() => api(`/admin/staff/${member.id}`, { method: "PUT", json: { fullName, role, active } }))) {
      onDone();
    }
  }

  async function resetPassword(event: FormEvent) {
    event.preventDefault();
    setPasswordSaved(false);
    if (await reset.run(() => api(`/admin/staff/${member.id}/password`, { method: "PUT", json: { password } }))) {
      setPassword("");
      setPasswordSaved(true);
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <form onSubmit={submit} className="flex flex-col gap-3">
        <ErrorAlert message={details.error} />
        <TextField label="Full name" required maxLength={120} value={fullName} onChange={(e) => setFullName(e.target.value)} />
        <SelectField label="Role" value={role} onChange={(e) => setRole(e.target.value as Role)}>
          <option value="STAFF">Staff</option>
          <option value="ADMIN">Admin</option>
        </SelectField>
        <CheckboxField label="Can sign in" checked={active} onChange={(e) => setActive(e.target.checked)} />
        <Button type="submit" disabled={details.busy}>
          Save
        </Button>
      </form>
      <form onSubmit={resetPassword} className="flex flex-col gap-3 border-t border-line pt-4">
        <ErrorAlert message={reset.error} />
        <SavedAlert message={passwordSaved ? "Password changed." : null} />
        <TextField label="New password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <Button type="submit" variant="secondary" disabled={reset.busy}>
          Change password
        </Button>
      </form>
    </div>
  );
}

export default function StaffPage() {
  const session = useSession();
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<UserAccount>>(`/admin/staff?page=${page}`);
  const [editing, setEditing] = useState<UserAccount | "new" | null>(null);
  const remove = useAction();

  if (session?.role !== "ADMIN") {
    return <p className="text-muted">Only admins can manage staff.</p>;
  }

  return (
    <>
      <PageHeader title="Staff" description="People who can sign in to this dashboard." action={<Button onClick={() => setEditing("new")}>Add staff member</Button>} />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Name", "Email", "Role", "Status", ""]}>
        {data?.content.map((member) => (
          <tr key={member.id}>
            <Cell className="font-medium">{member.fullName}</Cell>
            <Cell>{member.email}</Cell>
            <Cell>{member.role === "ADMIN" ? "Admin" : "Staff"}</Cell>
            <Cell>
              <StatusBadge tone={member.active ? "good" : "bad"}>{member.active ? "Active" : "Disabled"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(member)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/staff/${member.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No staff yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add staff member" : `Edit ${editing.fullName}`} onClose={() => setEditing(null)}>
          {editing === "new" ? (
            <NewStaffForm
              onDone={() => {
                setEditing(null);
                reload();
              }}
            />
          ) : (
            <EditStaffForm
              member={editing}
              onDone={() => {
                setEditing(null);
                reload();
              }}
            />
          )}
        </Modal>
      )}
    </>
  );
}
