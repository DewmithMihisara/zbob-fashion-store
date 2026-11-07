function formatDate(iso: string | null): string {
  return iso ? new Intl.DateTimeFormat("en-US", { month: "short", day: "numeric", year: "numeric", timeZone: "UTC" }).format(new Date(iso)) : "";
}

export function PostMeta({ minutes, date, className = "text-muted" }: { minutes: number; date: string | null; className?: string }) {
  return (
    <p className={`flex items-center gap-3 text-[15px] font-medium ${className}`}>
      <span>{minutes} min read</span>
      <span aria-hidden="true" className="h-4 w-px bg-current opacity-30" />
      <time dateTime={date ?? undefined}>{formatDate(date)}</time>
    </p>
  );
}
