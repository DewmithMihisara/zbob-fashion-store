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
  const colors = tone === "dark" ? "bg-gradient-to-b from-neutral-600 to-neutral-900" : "bg-tile text-ink/15";
  return (
    <div
      role="img"
      aria-label={label ? `${label} (photo coming soon)` : "Photo coming soon"}
      className={`@container flex size-full items-center justify-center select-none ${colors} ${className}`}
    >
      {tone === "light" && (
        <span aria-hidden="true" className="font-logo text-[clamp(12px,22cqw,48px)] uppercase">
          zbob
        </span>
      )}
    </div>
  );
}
