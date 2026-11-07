import type { ReactNode } from "react";

export function PageHeader({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <header className="mb-6 flex flex-wrap items-start justify-between gap-3">
      <div>
        <h1 className="text-[24px] font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 max-w-[640px] text-[14px] text-muted">{description}</p>}
      </div>
      {action}
    </header>
  );
}
