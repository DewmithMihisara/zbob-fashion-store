"use client";

import type { BlogPost } from "@zbob/shared-types";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use, useState } from "react";
import { ErrorAlert } from "@/components/ui/Alert";
import { ConfirmButton } from "@/components/ui/ConfirmButton";
import { ImageUploadButton } from "@/components/ui/ImageUploadButton";
import { PageHeader } from "@/components/ui/PageHeader";
import { PostForm } from "@/features/blog/PostForm";
import { useAction } from "@/hooks/useAction";
import { useData } from "@/hooks/useData";
import { api } from "@/lib/api";

export default function EditPostPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { data: post, error, reload } = useData<BlogPost>(`/admin/blog/${id}`);
  const action = useAction();
  const [fileProblem, setFileProblem] = useState<string | null>(null);

  async function uploadCover(file: File) {
    setFileProblem(null);
    const form = new FormData();
    form.append("file", file);
    await action.run(() => api(`/admin/blog/${id}/cover`, { method: "PUT", form }));
    reload();
  }

  async function remove() {
    if (await action.run(() => api(`/admin/blog/${id}`, { method: "DELETE" }))) {
      router.push("/blog");
    }
  }

  return (
    <div className="max-w-[760px]">
      <Link href="/blog" className="text-[13px] text-muted hover:underline">
        Back to blog
      </Link>
      <PageHeader title={post?.title ?? "Post"} action={post ? <ConfirmButton onConfirm={remove}>Delete post</ConfirmButton> : undefined} />
      <ErrorAlert message={error ?? action.error ?? fileProblem} />
      {post && (
        <div className="flex flex-col gap-5">
          <section className="flex flex-wrap items-center gap-4 rounded-xl border border-line bg-paper p-5">
            <div className="h-24 w-36 overflow-hidden rounded-lg bg-tile">
              {post.coverUrl ? (
                <img src={post.coverUrl} alt="Cover" className="size-full object-cover" />
              ) : (
                <span className="flex size-full items-center justify-center text-[12px] text-faint">No cover</span>
              )}
            </div>
            <ImageUploadButton disabled={action.busy} onFiles={([file]) => uploadCover(file!)} onProblem={setFileProblem}>
              {post.coverUrl ? "Replace cover" : "Upload cover"}
            </ImageUploadButton>
          </section>
          <PostForm key={post.id} post={post} onSaved={reload} />
        </div>
      )}
    </div>
  );
}
