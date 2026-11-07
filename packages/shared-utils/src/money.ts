/** Formats an integer amount of cents as a display price, e.g. 3500 -> "$35.00". */
export function formatPrice(cents: number, currency = "USD"): string {
  return new Intl.NumberFormat("en-US", { style: "currency", currency }).format(cents / 100);
}

/** Reads a typed price such as "35" or "35.5" as cents. Returns null when it is not a valid price. */
export function parsePrice(text: string): number | null {
  const value = text.trim();
  if (!/^\d+(\.\d{1,2})?$/.test(value)) {
    return null;
  }
  const [whole = "0", fraction = ""] = value.split(".");
  return Number(whole) * 100 + Number(fraction.padEnd(2, "0"));
}

/** Shows cents in a form input, e.g. 3550 -> "35.50". */
export function centsToInput(cents: number | null | undefined): string {
  return cents === null || cents === undefined ? "" : (cents / 100).toFixed(2);
}
