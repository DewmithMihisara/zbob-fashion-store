import type { CheckoutQuote } from "@zbob/shared-types";
import { formatPrice } from "@zbob/shared-utils";
import Link from "next/link";

export function CheckoutSummary({ quote }: { quote: CheckoutQuote }) {
  return (
    <aside className="flex flex-col gap-3 rounded-card bg-tile p-5" aria-label="Order summary">
      <h2 className="text-[22px] font-medium tracking-tight-3">Summary</h2>
      <ul className="flex flex-col gap-2 text-[15px]">
        {quote.lines.map((line) => (
          <li key={line.variantId} className="flex justify-between gap-3">
            <span>
              {line.productName}
              <span className="block text-muted">
                {line.size} / {line.color} · Qty {line.quantity}
              </span>
              {!line.available && <span className="block text-red-700">Not enough stock</span>}
            </span>
            <span className="font-medium">{formatPrice(line.lineTotalCents)}</span>
          </li>
        ))}
      </ul>
      <div className="flex flex-col gap-1 border-t border-line pt-3 text-[16px]">
        <p className="flex justify-between">
          <span>Subtotal</span>
          <span data-testid="checkout-subtotal">{formatPrice(quote.subtotalCents)}</span>
        </p>
        {quote.discountCents > 0 && (
          <p className="flex justify-between text-green-800">
            <span>Discount ({quote.promotionCode})</span>
            <span data-testid="checkout-discount">-{formatPrice(quote.discountCents)}</span>
          </p>
        )}
        <p className="flex justify-between">
          <span>Delivery</span>
          <span>{quote.shippingMethod ? formatPrice(quote.shippingCents) : "Choose an option"}</span>
        </p>
        <p className="mt-1 flex justify-between border-t border-line pt-2 text-[18px] font-medium">
          <span>Total</span>
          <span data-testid="checkout-total">{formatPrice(quote.totalCents)}</span>
        </p>
      </div>
      {quote.problems.length > 0 && (
        <ul className="flex flex-col gap-1 rounded-[10px] bg-paper p-3 text-[14px] text-muted" aria-label="Still needed">
          {quote.problems.map((problem) => (
            <li key={problem}>{problem}</li>
          ))}
        </ul>
      )}
      {/* Connect the payment gateway here. The API only allows paying once readyToPay is true. */}
      <button type="button" disabled={!quote.readyToPay} className="mt-2 h-12 w-full rounded-full bg-ink text-[17px] font-medium text-paper disabled:bg-faint">
        {quote.paymentAvailable ? "Pay now" : "Payment coming soon"}
      </button>
      {!quote.paymentAvailable && (
        <p className="text-center text-[14px] text-muted">
          Online payment is not open yet.{" "}
          <Link href="/contact" className="text-ink underline">
            Contact us
          </Link>{" "}
          to place this order.
        </p>
      )}
    </aside>
  );
}
