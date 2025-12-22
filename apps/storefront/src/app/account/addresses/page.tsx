"use client";

import type { CustomerAddress } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, FormField } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerData } from "@/hooks/useCustomerData";
import { customerApi } from "@/lib/customer-api";

function AddressForm({ address, onDone, onCancel }: { address: CustomerAddress | null; onDone: () => void; onCancel: () => void }) {
  const [recipient, setRecipient] = useState(address?.recipient ?? "");
  const [line1, setLine1] = useState(address?.line1 ?? "");
  const [line2, setLine2] = useState(address?.line2 ?? "");
  const [city, setCity] = useState(address?.city ?? "");
  const [region, setRegion] = useState(address?.region ?? "");
  const [postalCode, setPostalCode] = useState(address?.postalCode ?? "");
  const [country, setCountry] = useState(address?.country ?? "");
  const [phone, setPhone] = useState(address?.phone ?? "");
  const { run, busy, error, fieldErrors } = useCustomerAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const json = {
      recipient,
      line1,
      line2: line2 || null,
      city,
      region: region || null,
      postalCode,
      country: country.trim().toUpperCase(),
      phone: phone || null,
    };
    const ok = await run(() =>
      address
        ? customerApi(`/me/addresses/${address.id}`, { method: "PUT", json })
        : customerApi("/me/addresses", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-4 rounded-card bg-tile p-[22px]">
      <h2 className="text-[22px] font-medium tracking-tight-3">{address ? "Edit address" : "Add an address"}</h2>
      <ErrorNotice message={error} />
      <div className="grid gap-4 tablet:grid-cols-2">
        <FormField label="Recipient" required maxLength={120} autoComplete="name" value={recipient} onChange={(e) => setRecipient(e.target.value)} error={fieldErrors.recipient} />
        <FormField label="Phone" maxLength={30} autoComplete="tel" value={phone} onChange={(e) => setPhone(e.target.value)} error={fieldErrors.phone} />
        <FormField label="Address line 1" required maxLength={200} autoComplete="address-line1" value={line1} onChange={(e) => setLine1(e.target.value)} error={fieldErrors.line1} />
        <FormField label="Address line 2" maxLength={200} autoComplete="address-line2" value={line2} onChange={(e) => setLine2(e.target.value)} />
        <FormField label="City" required maxLength={100} value={city} onChange={(e) => setCity(e.target.value)} error={fieldErrors.city} />
        <FormField label="Region" maxLength={100} value={region} onChange={(e) => setRegion(e.target.value)} />
        <FormField label="Postal code" required maxLength={20} autoComplete="postal-code" value={postalCode} onChange={(e) => setPostalCode(e.target.value)} error={fieldErrors.postalCode} />
        <FormField label="Country code" required maxLength={2} placeholder="LK" hint="Two letters, such as LK." value={country} onChange={(e) => setCountry(e.target.value)} error={fieldErrors.country} />
      </div>
      <div className="flex flex-wrap gap-2">
        <button type="submit" disabled={busy} className={buttonClass("dark", "disabled:opacity-60")}>
          {busy ? "Saving..." : "Save address"}
        </button>
        <button type="button" onClick={onCancel} className={buttonClass("light")}>
          Cancel
        </button>
      </div>
    </form>
  );
}

export default function AddressesPage() {
  const { data, error, reload } = useCustomerData<CustomerAddress[]>("/me/addresses");
  const [editing, setEditing] = useState<CustomerAddress | "new" | null>(null);
  const remove = useCustomerAction();

  function done() {
    setEditing(null);
    reload();
  }

  return (
    <div className="flex flex-col gap-4">
      <ErrorNotice message={error ?? remove.error} />
      {editing ? (
        <AddressForm key={editing === "new" ? "new" : editing.id} address={editing === "new" ? null : editing} onDone={done} onCancel={() => setEditing(null)} />
      ) : (
        <div>
          <button type="button" onClick={() => setEditing("new")} className={buttonClass("dark")}>
            Add an address
          </button>
        </div>
      )}
      {data?.length === 0 && !editing && <p className="py-10 text-center text-muted">You have no saved addresses yet.</p>}
      <ul className="grid gap-4 tablet:grid-cols-2">
        {data?.map((address) => (
          <li key={address.id} className="rounded-card bg-tile p-5 text-[16px] leading-[24px]">
            <p className="font-medium">{address.recipient}</p>
            <p>{address.line1}</p>
            {address.line2 && <p>{address.line2}</p>}
            <p>
              {address.city}
              {address.region ? `, ${address.region}` : ""} {address.postalCode}
            </p>
            <p>{address.country}</p>
            {address.phone && <p className="text-muted">{address.phone}</p>}
            <div className="mt-3 flex gap-4 text-[15px]">
              <button type="button" onClick={() => setEditing(address)} className="underline">
                Edit
              </button>
              <button
                type="button"
                disabled={remove.busy}
                onClick={() => remove.run(() => customerApi(`/me/addresses/${address.id}`, { method: "DELETE" })).then(reload)}
                className="text-muted underline hover:text-ink"
              >
                Remove
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
