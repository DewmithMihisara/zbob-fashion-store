"use client";

import type { UserAccount } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField, SavedNotice } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerData } from "@/hooks/useCustomerData";
import { customerApi } from "@/lib/customer-api";
import { getCustomerSession, setCustomerSession } from "@/lib/customer-session";

function ProfileForm({ profile }: { profile: UserAccount }) {
  const [fullName, setFullName] = useState(profile.fullName);
  const [saved, setSaved] = useState(false);
  const { run, busy, error, fieldErrors } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSaved(false);
    await run(async () => {
      const updated = await customerApi<UserAccount>("/me", { method: "PUT", json: { fullName } });
      const session = getCustomerSession();
      if (session) {
        setCustomerSession({ ...session, fullName: updated.fullName });
      }
      setSaved(true);
    });
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h2 className="text-[22px] font-medium tracking-tight-3">Your details</h2>
      <ErrorNotice message={error} />
      <SavedNotice message={saved ? "Your details were saved." : null} />
      <FormField label="Full name" required maxLength={120} autoComplete="name" value={fullName} onChange={(e) => setFullName(e.target.value)} error={fieldErrors.fullName} />
      <FormField label="Email" type="email" value={profile.email} disabled hint="Contact us to change your email." />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Saving..." : "Save details"}
      </button>
    </form>
  );
}

function PasswordForm() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [saved, setSaved] = useState(false);
  const { run, busy, error, fieldErrors } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSaved(false);
    const ok = await run(() => customerApi("/me/password", { method: "PUT", json: { currentPassword, newPassword } }));
    if (ok) {
      setCurrentPassword("");
      setNewPassword("");
      setSaved(true);
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h2 className="text-[22px] font-medium tracking-tight-3">Change password</h2>
      <ErrorNotice message={error} />
      <SavedNotice message={saved ? "Your password was changed." : null} />
      <FormField label="Current password" type="password" required autoComplete="current-password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} error={fieldErrors.currentPassword} />
      <FormField label="New password" type="password" required minLength={8} maxLength={72} autoComplete="new-password" hint="At least 8 characters." value={newPassword} onChange={(e) => setNewPassword(e.target.value)} error={fieldErrors.newPassword} />
      <button type="submit" disabled={busy} className={buttonClass("dark", "h-12 disabled:opacity-60")}>
        {busy ? "Saving..." : "Change password"}
      </button>
    </form>
  );
}

export default function ProfilePage() {
  const { data, error } = useCustomerData<UserAccount>("/me");

  return (
    <>
      <ErrorNotice message={error} />
      {data && (
        <div className="grid items-start gap-5 tablet:grid-cols-2">
          <ProfileForm profile={data} />
          <PasswordForm />
        </div>
      )}
    </>
  );
}
