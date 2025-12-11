import type { Page } from "@zbob/shared-types";
import { Button } from "./Button";

export function Pagination({ page, onChange }: { page: Page<unknown> | undefined; onChange: (page: number) => void }) {
  if (!page || (page.totalPages <= 1 && page.page === 0)) {
    return null;
  }
  return (
    <nav aria-label="Pages" className="mt-4 flex items-center justify-between text-[13px] text-muted">
      <span>
        Page {page.page + 1} of {Math.max(page.totalPages, 1)} ({page.totalElements} total)
      </span>
      <span className="flex gap-2">
        <Button variant="secondary" disabled={page.page === 0} onClick={() => onChange(page.page - 1)}>
          Previous
        </Button>
        <Button variant="secondary" disabled={page.page + 1 >= page.totalPages} onClick={() => onChange(page.page + 1)}>
          Next
        </Button>
      </span>
    </nav>
  );
}
