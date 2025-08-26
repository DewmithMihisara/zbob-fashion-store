import type { Metadata } from "next";
import { CommunitySection } from "@/components/home/CommunitySection";
import { ProductGrid } from "@/components/product/ProductGrid";
import { LinkButton } from "@/components/ui/Button";
import { FilterTabs } from "@/components/ui/FilterTabs";
import { PageHero } from "@/components/ui/PageHero";
import { Container } from "@/components/ui/SectionHeading";
import { getCategories, getProducts, getSiteMedia } from "@/lib/api";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "Shop",
  description: "Browse everyday clothing for men, women and children.",
};

const PAGE_SIZE = 60;

export default async function ShopPage({ searchParams }: { searchParams: Promise<{ category?: string }> }) {
  const { category } = await searchParams;
  const [products, categories, media] = await Promise.all([
    getProducts({ category, size: PAGE_SIZE }),
    getCategories(),
    getSiteMedia(),
  ]);

  const tabs = [
    { href: "/shop", label: "All Products", active: !category },
    ...categories.map((c) => ({ href: `/shop?category=${c.slug}`, label: c.name, active: category === c.slug })),
  ];

  return (
    <>
      <PageHero
        lead="Shop"
        label="This season"
        photo={media["shop-hero"]}
        title={
          <>
            Everything you need,
            <br />
            nothing you don&apos;t
          </>
        }
        text="Browse pieces designed to mix, match and stay in your wardrobe for years, not months."
      >
        <LinkButton href="/blog" variant="light">
          Explore stories
        </LinkButton>
        <LinkButton href="/about" variant="glass">
          About us
        </LinkButton>
      </PageHero>
      <section className="pt-[86px]">
        <Container>
          <FilterTabs label="Filter products by category" tabs={tabs} />
          <div className="mt-8">
            <ProductGrid products={products.content} />
          </div>
        </Container>
      </section>
      <CommunitySection media={media} />
    </>
  );
}
