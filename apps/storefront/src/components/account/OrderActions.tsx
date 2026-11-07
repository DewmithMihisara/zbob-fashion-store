"use client";

import type { OrderDetail, ReturnRequest } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { buttonClass } from "@/components/ui/Button";
import { ErrorNotice, inputClass } from "@/components/ui/FormField";
import { useCustomerAction } from "@/hooks/useCustomerAction";
import { useCustomerData } from "@/hooks/useCustomerData";
import { customerApi } from "@/lib/customer-api";

const RETURN_LABELS: Record<ReturnRequest["status"], string> = {
  REQUESTED: "Waiting for a reply",
  APPROVED: "Approved",
  REJECTED: "Declined",
  COMPLETED: "Completed",
};

/** Cancel before payment, or ask for a return once delivered. Each is checked again by the API. */
export function OrderActions({ order, onChanged }: { order: OrderDetail; onChanged: () => void }) {
  const { data: returns, reload } = useCustomerData<ReturnRequest[]>(`/orders/${order.id}/returns`);
  const [reason, setReason] = useState("");
  const [confirming, setConfirming] = useState(false);
  const cancel = useCustomerAction();
  const returnAction = useCustomerAction();
  const hasOpenReturn = returns?.some((r) => r.status === "REQUESTED" || r.status === "APPROVED");

  async function requestReturn(event: FormEvent) {
    event.preventDefault();
    if (await returnAction.run(() => customerApi(`/orders/${order.id}/returns`, { method: "POST", json: { reason } }))) {
      setReason("");
      reload();
    }
  }

  return (
    <div className="flex flex-col gap-4">
      {order.status === "PENDING_PAYMENT" && (
        <div className="flex flex-col gap-2 rounded-card bg-tile p-5">
          <ErrorNotice message={cancel.error} />
          {confirming ? (
            <div className="flex flex-wrap items-center gap-3">
              <span className="text-[16px]">Cancel this order?</span>
              <button type="button" disabled={cancel.busy} onClick={() => cancel.run(() => customerApi(`/orders/${order.id}/cancel`, { method: "POST" })).then((ok) => ok && onChanged())} className={buttonClass("dark")}>
                Yes, cancel it
              </button>
              <button type="button" onClick={() => setConfirming(false)} className={buttonClass("light")}>
                Keep order
              </button>
            </div>
          ) : (
            <button type="button" onClick={() => setConfirming(true)} className={buttonClass("light", "self-start")}>
              Cancel order
            </button>
          )}
        </div>
      )}

      {returns && returns.length > 0 && (
        <section className="rounded-card bg-tile p-5">
          <h3 className="mb-2 text-[18px] font-medium">Returns</h3>
          <ul className="flex flex-col gap-2 text-[15px]">
            {returns.map((r) => (
              <li key={r.id}>
                <span className="font-medium">{RETURN_LABELS[r.status]}</span> · {r.reason}
                {r.staffNote && <span className="block text-muted">Note from zbob: {r.staffNote}</span>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {order.status === "DELIVERED" && !hasOpenReturn && (
        <form onSubmit={requestReturn} className="flex flex-col gap-3 rounded-card bg-tile p-5">
          <h3 className="text-[18px] font-medium">Return items</h3>
          <ErrorNotice message={returnAction.error} />
          <label className="block text-[16px] font-medium">
            What would you like to return, and why?
            <textarea required maxLength={1000} rows={3} value={reason} onChange={(e) => setReason(e.target.value)} className={inputClass} />
          </label>
          <button type="submit" disabled={returnAction.busy} className={buttonClass("dark", "self-start disabled:opacity-60")}>
            {returnAction.busy ? "Sending..." : "Request a return"}
          </button>
        </form>
      )}
    </div>
  );
}
