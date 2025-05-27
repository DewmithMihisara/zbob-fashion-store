/** Formats an integer amount of cents as a display price, e.g. 3500 -> "$35.00". */
export function formatPrice(cents: number, currency = "USD"): string {
  return new Intl.NumberFormat("en-US", { style: "currency", currency }).format(cents / 100);
}
