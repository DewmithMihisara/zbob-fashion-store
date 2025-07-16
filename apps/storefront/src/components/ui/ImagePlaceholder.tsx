/** Shown until a photo is uploaded in the admin dashboard. */
export function ImagePlaceholder({
  label,
  tone = "light",
  className = "",
}: {
  label?: string;
  tone?: "light" | "dark";
  className?: string;
}) {
  const colors =
    tone === "dark"
      ? "bg-gradient-to-b from-neutral-600 via-neutral-800 to-black text-paper/25"
      : "bg-tile text-ink/15";
  return (
    <div
      role="img"
      aria-label={label ? `${label} (photo coming soon)` : "Photo coming soon"}
      className={`flex size-full items-center justify-center select-none ${colors} ${className}`}
    >
      <span aria-hidden="true" className="font-logo text-[clamp(24px,5vw,48px)] uppercase">
        zbob
      </span>
    </div>
  );
}
