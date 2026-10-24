"use client";

import type { CustomerAddress, ShippingMethod } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";
import { useState } from "react";

const card = "flex cursor-pointer items-start gap-3 rounded-[10px] bg-paper p-4 has-[:checked]:outline-2 has-[:checked]:outline-ink";

export function AddressChoice({
  addresses,
  value,
  onChange,
  returnTo,
}: {
  addresses: CustomerAddress[];
  value: number | null;
  onChange: (id: number) => void;
  returnTo: string;
}) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="mb-2 text-[20px] font-medium tracking-tight-3">Delivery address</legend>
      {addresses.length === 0 && <p className="text-[15px] text-muted">You have no saved addresses yet.</p>}
      {addresses.map((address) => (
        <label key={address.id} className={card}>
          <input type="radio" name="address" className="mt-1 accent-black" checked={value === address.id} onChange={() => onChange(address.id)} />
          <span className="text-[15px] leading-[22px]">
            <span className="block font-medium">{address.recipient}</span>
            {address.line1}
            {address.line2 ? `, ${address.line2}` : ""}, {address.city} {address.postalCode}, {address.country}
          </span>
        </label>
      ))}
      <Link href={`/account/addresses?next=${encodeURIComponent(returnTo)}`} className="self-start text-[15px] underline">
        Add an address
      </Link>
    </fieldset>
  );
}

export function ShippingChoice({ methods, value, onChange }: { methods: ShippingMethod[]; value: number | null; onChange: (id: number) => void }) {
  return (
    <fieldset className="flex flex-col gap-2">
      <legend className="mb-2 text-[20px] font-medium tracking-tight-3">Delivery option</legend>
      {methods.length === 0 && <p className="text-[15px] text-muted">Delivery options are not set up yet.</p>}
      {methods.map((method) => (
        <label key={method.id} className={card}>
          <input type="radio" name="shipping" className="mt-1 accent-black" checked={value === method.id} onChange={() => onChange(method.id)} />
          <span className="flex flex-1 justify-between gap-3 text-[15px]">
            <span>
              <span className="block font-medium">{method.name}</span>
              <span className="text-muted">
                {method.description ? `${method.description} · ` : ""}About {method.estimatedDays} {method.estimatedDays === 1 ? "day" : "days"}
              </span>
            </span>
            <span className="font-medium">{method.priceCents === 0 ? "Free" : formatPrice(method.priceCents)}</span>
          </span>
        </label>
      ))}
    </fieldset>
  );
}

export function PromoCode({ applied, error, onApply }: { applied: string | null; error: string | null; onApply: (code: string) => void }) {
  const [code, setCode] = useState(applied ?? "");
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onApply(code.trim());
      }}
      className="flex flex-col gap-2"
    >
      <label htmlFor="promo" className="text-[20px] font-medium tracking-tight-3">
        Promo code
      </label>
      <div className="flex gap-2">
        <input
          id="promo"
          value={code}
          maxLength={40}
          onChange={(e) => setCode(e.target.value)}
          className="h-11 min-w-0 flex-1 rounded-full bg-paper px-4 text-[16px] uppercase outline-none focus:outline-2 focus:outline-ink"
        />
        <button type="submit" className="h-11 rounded-full bg-ink px-5 text-[16px] font-medium text-paper">
          Apply
        </button>
      </div>
      {applied && <p className="text-[14px] text-green-800">{applied} applied.</p>}
      {error && <p className="text-[14px] text-red-700">{error}</p>}
    </form>
  );
}
