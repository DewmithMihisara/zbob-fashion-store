"use client";

import type { CheckoutQuote, CustomerAddress, ShippingMethod } from "@zbob/shared-types";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { ErrorNotice } from "@/components/ui/FormField";
import { useCustomerData } from "@/hooks/useCustomerData";
import { useCustomerSession, useIsClient } from "@/hooks/useCustomerSession";
import { customerApi, customerErrorMessage } from "@/lib/customer-api";
import { AddressChoice, PromoCode, ShippingChoice } from "./CheckoutChoices";
import { CheckoutSummary } from "./CheckoutSummary";

interface Choices {
  addressId: number | null;
  shippingMethodId: number | null;
  promotionCode: string;
}

export function CheckoutView() {
  const router = useRouter();
  const pathname = usePathname();
  const params = useSearchParams();
  const isClient = useIsClient();
  const session = useCustomerSession();
  const variant = Number(params.get("variant"));
  const qty = Math.min(99, Math.max(1, Number(params.get("qty")) || 1));
  const buyNow = Number.isInteger(variant) && variant > 0 ? { variantId: variant, quantity: qty } : null;
  const returnTo = `${pathname}${params.size ? `?${params.toString()}` : ""}`;

  const { data: addresses } = useCustomerData<CustomerAddress[]>(session ? "/me/addresses" : "/shipping-methods");
  const { data: methods } = useCustomerData<ShippingMethod[]>("/shipping-methods");
  const [choices, setChoices] = useState<Choices>({ addressId: null, shippingMethodId: null, promotionCode: "" });
  const [quote, setQuote] = useState<{ key: string; data?: CheckoutQuote; error?: string } | null>(null);

  useEffect(() => {
    if (isClient && !session) {
      router.replace(`/login?next=${encodeURIComponent(returnTo)}`);
    }
  }, [isClient, session, returnTo, router]);

  const savedAddresses = session ? (addresses as CustomerAddress[] | undefined) : undefined;
  // The first saved address and cheapest delivery are picked for the customer, who can change them.
  const addressId = choices.addressId ?? savedAddresses?.[0]?.id ?? null;
  const shippingMethodId = choices.shippingMethodId ?? methods?.[0]?.id ?? null;
  const request = { addressId, shippingMethodId, promotionCode: choices.promotionCode || null, buyNow };
  const key = JSON.stringify(request);

  useEffect(() => {
    if (!session || savedAddresses === undefined || methods === undefined) {
      return;
    }
    let current = true;
    customerApi<CheckoutQuote>("/checkout/quote", { method: "POST", json: JSON.parse(key) }).then(
      (data) => {
        if (current) setQuote({ key, data });
      },
      (error: unknown) => {
        if (current) setQuote({ key, error: customerErrorMessage(error) });
      },
    );
    return () => {
      current = false;
    };
  }, [key, session, savedAddresses, methods]);

  if (!isClient || !session) {
    return null;
  }

  return (
    <div className="grid items-start gap-6 tablet:grid-cols-[1fr_380px]">
      <div className="flex flex-col gap-8">
        <ErrorNotice message={quote?.error} />
        <AddressChoice addresses={savedAddresses ?? []} value={addressId} onChange={(id) => setChoices({ ...choices, addressId: id })} returnTo={returnTo} />
        <ShippingChoice methods={methods ?? []} value={shippingMethodId} onChange={(id) => setChoices({ ...choices, shippingMethodId: id })} />
        <PromoCode
          applied={quote?.data?.promotionCode ?? null}
          error={quote?.data?.promotionError ?? null}
          onApply={(code) => setChoices({ ...choices, promotionCode: code })}
        />
      </div>
      {quote?.data ? <CheckoutSummary quote={quote.data} /> : <p className="text-muted">Working out your total...</p>}
    </div>
  );
}
