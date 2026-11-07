import type { Metadata } from "next";
import { PostCard } from "@/components/blog/PostCard";
import { CommunitySection } from "@/components/home/CommunitySection";
import { LinkButton } from "@/components/ui/Button";
import { FilterTabs } from "@/components/ui/FilterTabs";
import { PageHero } from "@/components/ui/PageHero";
import { Reveal } from "@/components/ui/Reveal";
import { getBlogPosts, getSiteMedia } from "@/lib/api";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Blog",
  description: "Style guides, fashion tips and stories from zbob.",
};

const TOPICS = ["Style Guide", "Fashion Tips", "Brand Stories"];

export default async function BlogPage({ searchParams }: { searchParams: Promise<{ topic?: string }> }) {
  const { topic } = await searchParams;
  const [posts, media] = await Promise.all([getBlogPosts({ topic, size: 30 }), getSiteMedia()]);
  const [lead, ...rest] = posts.content;

  const tabs = [
    { href: "/blog", label: "All Blogs", active: !topic },
    ...TOPICS.map((t) => ({ href: `/blog?topic=${encodeURIComponent(t)}`, label: t, active: topic === t })),
  ];

  return (
    <>
      <PageHero
        lead="Blog"
        label="Read our stories"
        photo={media["blog-hero"]}
        title={
          <>
            Notes on clothes
            <br />
            and how we wear them
          </>
        }
        text="Practical ideas about fabric, fit and building a wardrobe that works hard for you."
      >
        <LinkButton href="/shop" variant="light">
          Browse collections
        </LinkButton>
        <LinkButton href="/about" variant="glass">
          About us
        </LinkButton>
      </PageHero>
      <section className="pt-[86px]">
        <div className="mx-auto w-full max-w-[1230px] px-5">
          <FilterTabs label="Filter posts by topic" tabs={tabs} />
          {lead ? (
            <div className="mt-8 flex flex-col gap-5">
              <Reveal>
                <PostCard post={lead} large />
              </Reveal>
              <div className="grid gap-5 tablet:grid-cols-2">
                {rest.map((post) => (
                  <Reveal key={post.id}>
                    <PostCard post={post} />
                  </Reveal>
                ))}
              </div>
            </div>
          ) : (
            <p className="py-16 text-center text-[16px] text-muted">There are no posts on this topic yet.</p>
          )}
        </div>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
