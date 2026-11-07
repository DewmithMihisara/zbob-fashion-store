"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { PageHeader } from "@/components/ui/PageHeader";
import { PostForm } from "@/features/blog/PostForm";

export default function NewPostPage() {
  const router = useRouter();
  return (
    <div className="max-w-[760px]">
      <Link href="/blog" className="text-[13px] text-muted hover:underline">
        Back to blog
      </Link>
      <PageHeader title="Add post" description="After creating it, you can add a cover photo." />
      <PostForm onSaved={(post) => router.push(`/blog/${post.id}`)} />
    </div>
  );
}
