/** A dot in the stock row's colour. Light colours get an outline so they show on white. */
export function ColourSwatch({ hex }: { hex: string | null }) {
  if (!hex) {
    return null;
  }
  const value = Number.parseInt(hex.slice(1), 16);
  const brightness = ((value >> 16) & 255) * 0.299 + ((value >> 8) & 255) * 0.587 + (value & 255) * 0.114;
  return (
    <span
      aria-hidden="true"
      className={`inline-block size-4 shrink-0 rounded-full ${brightness > 200 ? "ring-1 ring-line" : ""}`}
      style={{ backgroundColor: hex }}
    />
  );
}
