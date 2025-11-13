"use client";

import type { BlogPostSummary, Page } from "@zbob/shared-types";
import Link from "next/link";
import { useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { PageHeader } from "@/components/ui/PageHeader";
import { Pagination } from "@/components/ui/Pagination";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Cell, EmptyRow, Table } from "@/components/ui/Table";
import { useData } from "@/hooks/useData";

export default function BlogPage() {
  const [page, setPage] = useState(0);
  const { data, error } = useData<Page<BlogPostSummary>>(`/admin/blog?page=${page}`);

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
      <ErrorAlert message={error} />
      <Table headers={["Cover", "Title", "Topic", "Status"]}>
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
          </tr>
        ))}
        {data?.content.length === 0 && <EmptyRow columns={4}>No posts yet.</EmptyRow>}
      </Table>
      <Pagination page={data} onChange={setPage} />
    </>
  );
}
