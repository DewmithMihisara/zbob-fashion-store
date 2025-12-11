"use client";

import type { BlogPostSummary, Page } from "@zbob/shared-types";
import Link from "next/link";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export default function BlogPage() {
  const [page, setPage] = useState(0);
  const { data, error, reload } = useData<Page<BlogPostSummary>>(`/admin/blog?page=${page}`);
  const remove = useAction();

  return (
    <>
      <PageHeader
        title="Blog"
        action={
          <Link href="/blog/new" className="inline-flex h-9 items-center rounded-lg bg-ink px-3 text-[13px] font-medium text-paper hover:bg-neutral-800">
            Add post
          </Link>
        }
      />
      <ErrorAlert message={error ?? remove.error} />
      <Table headers={["Cover", "Title", "Topic", "Status", ""]}>
        {data?.content.map((post) => (
          <tr key={post.id} className="hover:bg-canvas">
            <Cell>
              <div className="h-10 w-14 overflow-hidden rounded-md bg-tile">{post.coverUrl && <img src={post.coverUrl} alt="" className="size-full object-cover" />}</div>
            </Cell>
            <Cell>
              <Link href={`/blog/${post.id}`} className="font-medium hover:underline">
                {post.title}
              </Link>
            </Cell>
            <Cell>{post.topic}</Cell>
            <Cell>
              <StatusBadge tone={post.publishedAt ? "good" : "neutral"}>{post.publishedAt ? "Published" : "Draft"}</StatusBadge>
            </Cell>
            <Cell className="text-right">
              <span className="inline-flex items-center gap-1">
                <Link href={`/blog/${post.id}`} className="inline-flex h-9 items-center rounded-lg border border-line bg-paper px-3 text-[13px] font-medium hover:bg-canvas">
                  Edit
                </Link>
                <ConfirmButton confirmLabel="Yes" onConfirm={() => remove.run(() => api(`/admin/blog/${post.id}`, { method: "DELETE" })).then(reload)}>
                  Delete
                </ConfirmButton>
              </span>
            </Cell>
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={5}>No posts yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
