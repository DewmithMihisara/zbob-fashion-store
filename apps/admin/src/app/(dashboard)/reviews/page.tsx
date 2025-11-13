"use client";

import type { Page, Review } from "@zbob/shared-types";
import { useState, type FormEvent } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { Button } from "@/components/ui/Button";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { CheckboxField, SelectField, TextArea, TextField } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

function ReviewForm({ review, onDone }: { review: Review | null; onDone: () => void }) {
  const [authorName, setAuthorName] = useState(review?.authorName ?? "");
  const [authorTitle, setAuthorTitle] = useState(review?.authorTitle ?? "");
  const [headline, setHeadline] = useState(review?.headline ?? "");
  const [body, setBody] = useState(review?.body ?? "");
  const [rating, setRating] = useState(String(review?.rating ?? 5));
  const [approved, setApproved] = useState(review?.approved ?? false);
  const { run, busy, error, fieldErrors } = useAction();

  async function submit(event: FormEvent) {
    event.preventDefault();
    const json = { authorName, authorTitle: authorTitle || null, headline, body, rating: Number(rating), approved };
    const ok = await run(() =>
      review
        ? api(`/admin/reviews/${review.id}`, { method: "PUT", json })
        : api("/admin/reviews", { method: "POST", json }),
    );
    if (ok) {
      onDone();
    }
  }

  return (
    <form onSubmit={submit} className="flex flex-col gap-3">
      <ErrorAlert message={error} />
      <TextField label="Customer name" required maxLength={120} value={authorName} onChange={(e) => setAuthorName(e.target.value)} error={fieldErrors.authorName} />
      <TextField label="Customer title" maxLength={120} value={authorTitle} onChange={(e) => setAuthorTitle(e.target.value)} />
      <TextField label="Headline" required maxLength={200} value={headline} onChange={(e) => setHeadline(e.target.value)} error={fieldErrors.headline} />
      <TextArea label="Review" required rows={4} maxLength={2000} value={body} onChange={(e) => setBody(e.target.value)} error={fieldErrors.body} />
      <SelectField label="Rating" value={rating} onChange={(e) => setRating(e.target.value)}>
        {[5, 4, 3, 2, 1].map((value) => (
          <option key={value} value={value}>
            {value} out of 5
          </option>
        ))}
      </SelectField>
      <CheckboxField label="Approved (shown on the website and counted in the rating)" checked={approved} onChange={(e) => setApproved(e.target.checked)} />
      <Button type="submit" disabled={busy}>
        {busy ? "Saving..." : "Save"}
      </Button>
    </form>
  );
}

export default function ReviewsPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<Review>>(`/admin/reviews?page=${page}`);
  const [editing, setEditing] = useState<Review | "new" | null>(null);
  const action = useAction();

  return (
    <>
      <PageHeader title="Reviews" description="Only approved reviews appear on the website, and only they count towards the rating." action={<Button onClick={() => setEditing("new")}>Add review</Button>} />
      <ErrorAlert message={error ?? action.error} />
      <Table headers={["Customer", "Review", "Rating", "Status", ""]}>
        {data?.content.map((review) => (
          <tr key={review.id}>
            <Cell className="font-medium whitespace-nowrap">{review.authorName}</Cell>
            <Cell className="max-w-[360px]">
              <p className="font-medium">{review.headline}</p>
              <p className="line-clamp-2 text-muted">{review.body}</p>
            </Cell>
            <Cell>{review.rating}/5</Cell>
            <Cell>
              <StatusBadge tone={review.approved ? "good" : "warn"}>{review.approved ? "Approved" : "Waiting"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex gap-1">
                <Button variant="secondary" onClick={() => setEditing(review)}>
                  Edit
                </Button>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => action.run(() => api(`/admin/reviews/${review.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No reviews yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
      {editing && (
        <Modal title={editing === "new" ? "Add review" : "Edit review"} onClose={() => setEditing(null)}>
          <ReviewForm
            review={editing === "new" ? null : editing}
            onDone={() => {
              setEditing(null);
              reload();
            }}
          />
        </Modal>
      )}
    </>
  );
}
