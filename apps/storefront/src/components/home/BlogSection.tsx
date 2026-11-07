import type { BlogPostSummary } from "@zbob/shared-types";
import { PostCard } from "@/components/blog/PostCard";
import { LinkButton } from "@/components/ui/Button";
import { Reveal } from "@/components/ui/Reveal";
import { Container, SplitHeading } from "@/components/ui/SectionHeading";

export function BlogSection({ posts }: { posts: BlogPostSummary[] }) {
  const [lead, ...rest] = posts;
  if (!lead) {
    return null;
  }
  return (
    <section className="pt-[90px]">
      <Container>
        <SplitHeading
          title={
            <>
              Ideas for getting
              <br />
              dressed every day
            </>
          }
          action={<LinkButton href="/blog" className="self-start tablet:self-auto">Read all blogs</LinkButton>}
        />
        <div className="mt-[55px] flex flex-col gap-5">
          <Reveal>
            <PostCard post={lead} large />
          </Reveal>
          <div className="grid gap-5 tablet:grid-cols-2">
            {rest.slice(0, 2).map((post) => (
              <Reveal key={post.id}>
                <PostCard post={post} />
              </Reveal>
            ))}
          </div>
        </div>
      </Container>
    </section>
  );
}
