import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { PostBody } from "@/components/blog/PostBody";
import { PostMeta } from "@/components/blog/PostMeta";
import { CommunitySection } from "@/components/home/CommunitySection";
import { PageHero } from "@/components/ui/PageHero";
import { getBlogPost, getSiteMedia } from "@/lib/api";

export const dynamic = "force-dynamic";

type Props = { params: Promise<{ slug: string }> };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const post = await getBlogPost((await params).slug);
  return post ? { title: post.title, description: post.excerpt } : { title: "Not found" };
}

export default async function BlogPostPage({ params }: Props) {
  const [post, media] = await Promise.all([getBlogPost((await params).slug), getSiteMedia()]);
  if (!post) {
    notFound();
  }
  return (
    <>
      <PageHero lead="Topic:" label={post.topic} title={post.title} text={post.excerpt} photo={post.coverUrl}>
        <PostMeta minutes={post.readMinutes} date={post.publishedAt} className="text-paper" />
      </PageHero>
      <article className="mx-auto max-w-[830px] px-5 pt-[50px]">
        <PostBody body={post.body} />
      </article>
      <CommunitySection media={media} />
    </>
  );
}
