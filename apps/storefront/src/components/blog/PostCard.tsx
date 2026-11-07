import type { BlogPostSummary } from "@zbob/shared-types";
import Link from "next/link";
import { Photo } from "@/components/ui/Photo";
import { PostMeta } from "./PostMeta";

/** Large lead story, or a compact card: photo on one side, details on the other. */
export function PostCard({ post, large = false }: { post: BlogPostSummary; large?: boolean }) {
  return (
    <article className={`grid overflow-hidden rounded-card bg-tile ${large ? "tablet:grid-cols-2" : "grid-cols-1 tablet:grid-cols-[1fr_1fr]"}`}>
      <Link href={`/blog/${post.slug}`} className={`relative block overflow-hidden ${large ? "aspect-[575/480]" : "aspect-[283/250]"}`}>
        <Photo src={post.coverUrl} alt={post.title} sizes="(min-width: 810px) 50vw, 100vw" />
      </Link>
      <div className={`flex flex-col ${large ? "p-8" : "p-[18px]"}`}>
        <span className="self-start rounded-full bg-paper px-[15px] py-[6px] text-[14px]">{post.topic}</span>
        <h3 className={`mt-4 font-medium tracking-tight-3 ${large ? "text-[34px] leading-[1.15] tablet:text-[44px]" : "text-[22px] leading-[28px]"}`}>
          <Link href={`/blog/${post.slug}`}>{post.title}</Link>
        </h3>
        {large && <p className="mt-4 text-[16px] leading-[25px] font-medium text-muted">{post.excerpt}</p>}
        <div className="mt-auto pt-6">
          <PostMeta minutes={post.readMinutes} date={post.publishedAt} className={large ? "text-ink" : "text-muted"} />
        </div>
      </div>
    </article>
  );
}
